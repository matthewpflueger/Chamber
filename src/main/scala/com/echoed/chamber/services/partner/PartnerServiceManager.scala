package com.echoed.chamber.services.partner

import com.echoed.util.Encrypter
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
import java.util.{UUID, HashMap => JHashMap}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.actor.SupervisorStrategy.Restart


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
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {


    private val cache = cacheManager.getCache[ActorRef]("PartnerServices")

    override val supervisorStrategy = OneForOneStrategy(
            maxNrOfRetries = 10,
            withinTimeRange = 1 minute)(SupervisorStrategy.defaultStrategy.decider)


    def handle = {
        case Terminated(ref) => for ((k, v) <- cache if (v == ref)) cache.remove(k)

        case msg @ RegisterPartner(partner, partnerSettings, partnerUser) =>
            val me = context.self
            val channel = context.sender


            def error(e: Throwable) {
                log.error("Unexpected error processing %s" format msg, e)
                e match {
                    case e: DataIntegrityViolationException =>
                        channel ! RegisterPartnerResponse(msg, Left(PartnerException(e.getCause.getMessage, e)))
                    case e: PartnerException =>
                        channel ! RegisterPartnerResponse(msg, Left(e))
                    case e =>
                        channel ! RegisterPartnerResponse(msg, Left(PartnerException("Could not register %s" format partner.name, e)))
                }
            }

            try {

                (me ? LocateByDomain(partner.domain)).onComplete(_.fold(
                    error(_),
                    _ match {
                        case LocateByDomainResponse(_, Right(partnerService)) =>
                            log.debug("Partner already exists {}", partner.domain)
                            channel ! RegisterPartnerResponse(msg, Left(PartnerAlreadyExists(partnerService)))
                        case LocateByDomainResponse(_, Left(e: PartnerNotFound)) =>
                            val p = partner.copy(secret = encrypter.generateSecretKey)
                            val ps = partnerSettings.copy(partnerId = p.id)

                            val password = UUID.randomUUID().toString
                            val pu = partnerUser.copy(partnerId = p.id).createPassword(password)

                            val code = encrypter.encrypt("""{"email": "%s", "password": "%s"}""" format (pu.email, password))


                            transactionTemplate.execute({status: TransactionStatus =>
                                partnerDao.insert(p)
                                partnerSettingsDao.insert(ps)
                                partnerUserDao.insert(pu)
                            })

                            val model = new JHashMap[String, AnyRef]()
                            model.put("code", code)
                            model.put("partner", p)
                            model.put("partnerUser", pu)

                            mp(SendEmail(
                                    partnerUser.email,
                                    "Your Echoed Account",
                                    "partner_email_register",
                                    model))

                            (me ? Locate(partner.id)).onComplete(_.fold(
                                error(_),
                                _ match {
                                    case LocateResponse(_, Left(e)) => error(e)
                                    case LocateResponse(_, Right(partnerService)) =>
                                        channel ! RegisterPartnerResponse(msg, Right(partnerService))
                                }))
                        case LocateByDomainResponse(_, Left(e)) => error(e)
                    }))
            } catch {
                case e => error(e)
            }

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
                        Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
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

            val constructor = findResponseConstructor(msg.asInstanceOf[PartnerMessage])

            (me ? Locate(partnerId)).mapTo[LocateResponse].onComplete(_.fold(
                e => {
                    log.error("Unexpected error in locating partner {}: {}", partnerId, e)
                    channel ! constructor.newInstance(msg, Left(new PartnerException("Error locating partner %s" format partnerId, e)))
                },
                _ match {
                    case LocateResponse(Locate(partnerId), Left(e)) =>
                        log.error("Error locating partner {}: {}", partnerId, e)
                        channel ! constructor.newInstance(msg, Left(e))
                    case LocateResponse(_, Right(ps)) =>
                        log.debug("Located partner {}, forwarding on message {}", partnerId, msg)
                        ps.tell(msg, channel)
                }))


        case msg: EchoIdentifiable => //(echoId) =>
            val me = context.self
            val channel = context.sender

            val echoId = msg.echoId

            log.debug("Starting to locate partner for echo {}", echoId)

            val constructor = findResponseConstructor(msg.asInstanceOf[PartnerMessage])

            (me ? LocateByEchoId(echoId)).mapTo[LocateByEchoIdResponse].onComplete(_.fold(
                e => {
                    log.error("Unexpected error in locating partner for echo {}: {}", echoId, e)
                    channel ! constructor.newInstance(msg, Left(new PartnerException("Error locating with echo id %s" format echoId, e)))
                },
                _ match {
                    case LocateByEchoIdResponse(_, Left(e)) =>
                        log.error("Error in locating partner for echo {}: {}", echoId, e)
                        channel ! constructor.newInstance(msg, Left(e))
                    case LocateByEchoIdResponse(_, Right(ps)) =>
                        log.debug("Located partner for echo {}, forwarding on message {}", echoId, msg)
                        ps.tell(msg, channel)
                }))
    }


    private def findResponseConstructor(msg: PartnerMessage) = {
        val requestClass = msg.getClass
        val responseClass = Thread.currentThread.getContextClassLoader.loadClass(requestClass.getName + "Response")
        responseClass.getConstructor(requestClass, classOf[Either[_, _]])
    }

}



