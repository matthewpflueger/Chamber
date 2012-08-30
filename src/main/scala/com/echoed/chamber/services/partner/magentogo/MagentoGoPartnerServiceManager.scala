package com.echoed.chamber.services.partner.magentogo

import collection.JavaConversions._
import collection.mutable.ConcurrentMap
import com.echoed.cache.CacheManager
import com.echoed.chamber.services.partner._
import org.springframework.transaction.TransactionStatus
import com.echoed.chamber.dao._
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.email.SendEmail
import com.echoed.util.TransactionUtils._
import partner.magentogo.MagentoGoPartnerDao
import partner.{PartnerSettingsDao, PartnerDao, PartnerUserDao}
import scalaz._
import Scalaz._
import java.util.{HashMap, UUID}
import com.echoed.util.{ObjectUtils, Encrypter}
import com.echoed.chamber.domain.partner.magentogo.MagentoGoCredentials
import com.echoed.chamber.domain.partner.{PartnerSettings, PartnerUser, Partner}
import akka.actor._
import akka.util.duration._
import com.echoed.chamber.services.{MessageProcessor, EchoedService}
import akka.pattern._
import akka.util.Timeout


class MagentoGoPartnerServiceManager(
        mp: MessageProcessor,
        partnerDao: PartnerDao,
        partnerSettingsDao: PartnerSettingsDao,
        partnerUserDao: PartnerUserDao,
        echoDao: EchoDao,
        encrypter: Encrypter,
        transactionTemplate: TransactionTemplate,
        accountManagerEmail: String,
        cacheManager: CacheManager,

        magentoGoPartnerDao: MagentoGoPartnerDao,
        magentoGoPartnerServiceCreator: (ActorContext, String) => ActorRef,
        magentoGoAccessCreator: ActorContext => ActorRef,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {


    private var cache: ConcurrentMap[String, ActorRef] = cacheManager.getCache[ActorRef]("PartnerServices")

    override val supervisorStrategy = OneForOneStrategy(
            maxNrOfRetries = 10,
            withinTimeRange = 1 minute)(SupervisorStrategy.defaultStrategy.decider)

    private val magentoGoAccess = magentoGoAccessCreator(context)

    def handle = {
        case Terminated(ref) => for ((k, v) <- cache if (v == ref)) cache.remove(k)

        case Create(msg @ Locate(partnerId), channel) =>
            val partnerService = cache.get(partnerId).getOrElse {
                val ps = context.watch(magentoGoPartnerServiceCreator(context, partnerId))
                cache.put(partnerId, ps)
                ps
            }
            channel ! LocateResponse(msg, Right(partnerService))

        case msg @ Locate(partnerId) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) = e match {
                case pe: PartnerException => channel ! LocateResponse(msg, Left(pe))
                case _ => channel ! LocateResponse(
                    msg,
                    Left(PartnerException("Could not locate MagentoGo partner", e)))
                    log.error("Error processing %s" format msg, e)
            }

            try {
                cache.get(partnerId).cata(
                    partnerService => {
                        channel ! LocateResponse(msg, Right(partnerService))
                        log.debug("Cache hit for {}", partnerService)
                    },
                    {
                        Option(magentoGoPartnerDao.findByPartnerId(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                        Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                        me ! Create(msg, channel)
                    })
            } catch {
                case e => error(e)
            }

        case msg @ RegisterMagentoGoPartner(mg) =>
            val channel = context.sender

            def error(e: Throwable) = {
                e match {
                    case pe: PartnerException => channel ! RegisterMagentoGoPartnerResponse(msg, Left(pe))
                    case _ => channel ! RegisterMagentoGoPartnerResponse(
                        msg,
                        Left(MagentoGoPartnerException("Could not register MagentoGo partner", e)))
                        log.error("Error processing %s" format msg, e)
                }

                mp(SendEmail(
                    accountManagerEmail,
                    "Failed MagentoGo partner %s" format mg.businessName,
                    "magentogo_accountManager_register_email",
                    ObjectUtils.asMap(mg)))
            }

            try {
                val mgc = MagentoGoCredentials(mg.apiPath, mg.apiUser, mg.apiKey)
                (magentoGoAccess ? Validate(mgc)).onComplete(_.fold(
                    error(_),
                    _ match {
                        case ValidateResponse(_, Left(e)) => error(e)
                        case ValidateResponse(_, Right(_)) =>
                            val p = new Partner(
                                        name = mg.businessName,
                                        domain = mg.storeUrl,
                                        phone = mg.phone,
                                        logo = null,
                                        category = "Other",
                                        handle = null).copy(cloudPartnerId = "MagentoGo")
                            val mgp = mg.copy(partnerId = p.id)

                            val password = UUID.randomUUID().toString
                            val pu = new PartnerUser(p.id, mg.name, mg.email).createPassword(password)
                            val ps = PartnerSettings.createPartnerSettings(p.id)
                            val code = encrypter.encrypt("""{"email": "%s", "password": "%s"}""" format(pu.email, password))

                            log.debug("Creating MagentoGo partner service for {}, {}", p.name, pu.email)
                            transactionTemplate.execute({ status: TransactionStatus =>
                                partnerDao.insert(p)
                                partnerSettingsDao.insert(ps)
                                partnerUserDao.insert(pu)
                                magentoGoPartnerDao.insert(mgp)
                            })

                            channel ! RegisterMagentoGoPartnerResponse(
                                msg,
                                Right(RegisterMagentoGoPartnerEnvelope(mgp, p, pu)))


                            val model = new HashMap[String, AnyRef]()
                            model.put("code", code)
                            model.put("magentoGoPartner", mgp)
                            model.put("partner", p)
                            model.put("partnerUser", pu)

                            mp(SendEmail(
                                pu.email,
                                "Thank you for choosing Echoed",
                                "magentogo_partner_email_register",
                                model))

                            mp(SendEmail(
                                accountManagerEmail,
                                "New MagentoGo partner %s" format p.name,
                                "magentogo_accountManager_email",
                                model))
                    }))
            } catch {
                case e => error(e)
            }
    }

}
