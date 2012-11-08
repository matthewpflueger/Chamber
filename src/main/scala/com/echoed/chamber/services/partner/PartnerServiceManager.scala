package com.echoed.chamber.services.partner

import com.echoed.util.{DateUtils, Encrypter}
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.email.SendEmail
import com.echoed.util.TransactionUtils._
import scalaz._
import Scalaz._
import akka.dispatch.Future
import com.echoed.chamber.dao._
import partner.{PartnerDao, PartnerSettingsDao, PartnerUserDao}
import com.echoed.cache.CacheManager
import com.echoed.chamber.services.{MessageProcessor, EchoedService}
import java.util.{Date, UUID}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import com.echoed.chamber.services.state.{QueryUniqueResponse, QueryUnique}
import com.echoed.chamber.domain.partner.{PartnerSettings, Partner, PartnerUser}


class PartnerServiceManager(
        mp: MessageProcessor,
        partnerDao: PartnerDao,
        partnerSettingsDao: PartnerSettingsDao,
        partnerUserDao: PartnerUserDao,
        echoDao: EchoDao,
        encrypter: Encrypter,
        transactionTemplate: TransactionTemplate,
        cacheManager: CacheManager,
        partnerServiceCreator: (ActorContext, String) => ActorRef,
        accountManagerEmail: String = "accountmanager@echoed.com",
        accountManagerEmailTemplate: String = "partner_accountManager_email",
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {


    private val cache = cacheManager.getCache[ActorRef]("PartnerServices")

    override val supervisorStrategy = OneForOneStrategy(
            maxNrOfRetries = 10,
            withinTimeRange = 1 minute)(SupervisorStrategy.defaultStrategy.decider)


    def handle = {
        case Terminated(ref) => for ((k, v) <- cache if (v == ref)) cache.remove(k)

        case msg @ RegisterPartner(userName, email, siteName, siteUrl, shortName, community) =>
            mp.tell(QueryUnique(msg, msg, Option(sender)), self)

        case QueryUniqueResponse(QueryUnique(_, msg: RegisterPartner, Some(channel)), Left(e)) =>
            channel ! RegisterPartnerResponse(msg, Left(InvalidRegistration(e.asErrors())))

        case QueryUniqueResponse(QueryUnique(_, msg: RegisterPartner, Some(channel)), Right(true)) =>
            val p = new Partner(msg.siteName, msg.siteUrl, msg.shortName, msg.community).copy(secret = encrypter.generateSecretKey)
            val ps = new PartnerSettings(p.id, p.handle)

            val password = UUID.randomUUID().toString
            val pu = new PartnerUser(msg.userName, msg.email).copy(partnerId = p.id).createPassword(password)

            val code = encrypter.encrypt(
                    """{ "password": "%s", "createdOn": "%s" }"""
                    format(password, DateUtils.dateToLong(new Date)))

            transactionTemplate.execute({status: TransactionStatus =>
                partnerDao.insert(p)
                partnerSettingsDao.insert(ps)
                partnerUserDao.insert(pu)
            })

            channel ! RegisterPartnerResponse(msg, Right(pu, p))

            val model = Map(
                "code" -> code,
                "partner" -> p,
                "partnerUser" -> pu)

            mp(SendEmail(
                pu.email,
                "Your Echoed Account",
                "partner_email_register",
                model))

            mp(SendEmail(
                accountManagerEmail,
                "New partner %s" format p.name,
                accountManagerEmailTemplate,
                model))


        case Create(msg @ Locate(partnerId), channel) =>
            val partnerService = cache.get(partnerId).getOrElse {
                val ps = context.watch(partnerServiceCreator(context, partnerId))
                cache.put(partnerId, ps)
                ps
            }
            channel ! LocateResponse(msg, Right(partnerService))

        case msg @ Locate(partnerId) =>
            val ctx = context
            val me = context.self
            val channel = context.sender
            implicit val ec = context.dispatcher

            cache.get(partnerId) match {
                case Some(partnerService) =>
                    channel ! LocateResponse(msg, Right(partnerService))
                    log.debug("Cache hit for {}", partnerService)
                case _ =>
                    log.debug("Looking up partner {}", partnerId)
                    Future {
                        Option(partnerDao.findByIdOrHandle(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    }.onComplete(_.fold(
                        _ match {
                            case e: PartnerNotFound =>
                                log.debug("Partner not found {}", partnerId)
                                channel ! LocateResponse(msg, Left(e))
                            case e: PartnerException => channel ! LocateResponse(msg, Left(e))
                            case e => channel ! LocateResponse(msg, Left(PartnerException("Error locating partner %s" format partnerId, e)))
                        },
                        {
                            case partner if (Option(partner.cloudPartnerId) == None) => me ! Create(msg, channel)
                            case partner =>
                                log.debug("Found {} partner {}", partner.cloudPartnerId, partner.name)
                                context.actorFor("../%sPartners" format partner.cloudPartnerId).tell(msg, channel)
//                                cloudPartners(partner.cloudPartnerId).tell(msg, channel)
//                                cloudPartners.get(partner.cloudPartnerId).actorRef.tell(msg, channel)
                        }))
            }


        case msg @ LocateByEchoId(echoId) =>
            val me = context.self
            val channel = context.sender

            log.debug("Locating partner for echo {}", echoId)

            Option(echoDao.findByIdOrPostId(echoId)).cata(
                echo => ((me ? Locate(echo.partnerId)).mapTo[LocateResponse]).onComplete(_.fold(
                    e => {
                        log.error("Unexpected error in locating partner for echo {}: {}", echoId, e)
                        channel ! LocateByEchoIdResponse(msg, Left(PartnerException("Unexpected error", e)))
                    },
                    _ match {
                        case LocateResponse(_, Left(e)) =>
                            log.error("Error in locating partner for echo {}: {}", echoId, e)
                            channel ! LocateByEchoIdResponse(msg, Left(e))
                        case LocateResponse(_, Right(partnerService)) =>
                            log.debug("Found parnter for echo {}", echoId)
                            channel ! LocateByEchoIdResponse(msg, Right(partnerService))
                    })),
                {
                    log.error("Did not find partner for echo {}", echoId)
                    channel ! LocateByEchoIdResponse(msg, Left(EchoNotFound(echoId)))
                })


        case msg @ LocateByDomain(domain, _) =>
            val me = context.self
            val channel = context.sender

            Option(partnerDao.findByDomain(domain)).cata(
                partner => (me ? Locate(partner.id)).mapTo[LocateResponse].onComplete(_.fold(
                    e => channel ! LocateByDomainResponse(msg, Left(PartnerException("Unexpected error", e))),
                    _ match {
                        case LocateResponse(_, Left(e)) => channel ! LocateByDomainResponse(msg, Left(e))
                        case LocateResponse(_, Right(partnerService)) => channel ! LocateByDomainResponse(msg, Right(partnerService))
                    })),
                {
                    channel ! LocateByDomainResponse(msg, Left(PartnerNotFound(domain)))
                })

        case msg: PartnerIdentifiable => //(partnerId) =>
            val me = context.self
            val channel = context.sender

            val partnerId = msg.partnerId

            log.debug("Starting to locate partner {}", partnerId)

            (me ? Locate(partnerId)).mapTo[LocateResponse].onComplete(_.fold(
                e => {
                    log.error("Unexpected error in locating partner {}: {}", partnerId, e)
                },
                _ match {
                    case LocateResponse(Locate(partnerId), Left(e)) =>
                        log.error("Error locating partner {}: {}", partnerId, e)
                    case LocateResponse(_, Right(ps)) =>
                        log.debug("Located partner {}, forwarding on message {}", partnerId, msg)
                        ps.tell(msg, channel)
                }))


        case msg: EchoIdentifiable => //(echoId) =>
            val me = context.self
            val channel = context.sender

            val echoId = msg.echoId

            log.debug("Starting to locate partner for echo {}", echoId)

            (me ? LocateByEchoId(echoId)).mapTo[LocateByEchoIdResponse].onComplete(_.fold(
                e => {
                    log.error("Unexpected error in locating partner for echo {}: {}", echoId, e)
                },
                _ match {
                    case LocateByEchoIdResponse(_, Left(e)) =>
                        log.error("Error in locating partner for echo {}: {}", echoId, e)
                    case LocateByEchoIdResponse(_, Right(ps)) =>
                        log.debug("Located partner for echo {}, forwarding on message {}", echoId, msg)
                        ps.tell(msg, channel)
                }))
    }


}



