package com.echoed.chamber.services.partner.magentogo

import reflect.BeanProperty
import collection.JavaConversions._
import collection.mutable.ConcurrentMap
import com.echoed.cache.CacheManager
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.partner._
import org.springframework.transaction.TransactionStatus
import com.echoed.chamber.dao._
import com.echoed.chamber.services.image.ImageService
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.email.EmailService
import com.echoed.util.TransactionUtils._
import partner.magentogo.MagentoGoPartnerDao
import partner.{PartnerSettingsDao, PartnerDao, PartnerUserDao}
import scalaz._
import Scalaz._
import java.util.{Properties, HashMap, UUID}
import com.echoed.util.{ObjectUtils, Encrypter}
import com.echoed.chamber.domain.partner.magentogo.MagentoGoCredentials
import com.echoed.chamber.domain.partner.{PartnerSettings, PartnerUser, Partner}
import org.springframework.beans.factory.FactoryBean
import akka.actor._
import akka.actor.SupervisorStrategy.Restart
import akka.util.Timeout
import akka.util.duration._
import akka.event.{LoggingAdapter, Logging}


class MagentoGoPartnerServiceManagerActor(
        magentoGoAccess: MagentoGoAccess,
        magentoGoPartnerDao: MagentoGoPartnerDao,
        partnerDao: PartnerDao,
        partnerSettingsDao: PartnerSettingsDao,
        partnerUserDao: PartnerUserDao,
        echoDao: EchoDao,
        echoMetricsDao: EchoMetricsDao,
        imageDao: ImageDao,
        imageService: ImageService,
        encrypter: Encrypter,
        transactionTemplate: TransactionTemplate,
        emailService: EmailService,
        accountManagerEmail: String,
        cacheManager: CacheManager,
        partnerServiceManager: PartnerServiceManager) extends Actor with ActorLogging {

    //this will be replaced by the ActorRegistry eventually (I think)
    private var cache: ConcurrentMap[String, PartnerService] = cacheManager.getCache[PartnerService]("PartnerServices")

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    def receive = {

        case Create(msg @ Locate(partnerId), channel) =>
            val partnerService = cache.get(partnerId).getOrElse {
                val ps = new MagentoGoPartnerServiceActorClient(context.actorOf(Props().withCreator {
                    val mgp = Option(magentoGoPartnerDao.findByPartnerId(partnerId)).get
                    val p = Option(partnerDao.findById(partnerId)).get
                    log.debug("Found MagentoGo partner {}", mgp.name)
                    new MagentoGoPartnerServiceActor(
                        mgp,
                        p,
                        magentoGoAccess,
                        magentoGoPartnerDao,
                        partnerDao,
                        partnerSettingsDao,
                        echoDao,
                        echoMetricsDao,
                        imageDao,
                        imageService,
                        transactionTemplate,
                        encrypter)
                }, partnerId))
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
                        val mgp = Option(magentoGoPartnerDao.findByPartnerId(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                        val p = Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
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

                emailService.sendEmail(
                    accountManagerEmail,
                    "Failed MagentoGo partner %s" format mg.businessName,
                    "magentogo_accountManager_register_email",
                    ObjectUtils.asMap(mg))
            }

            try {
                val mgc = MagentoGoCredentials(mg.apiPath, mg.apiUser, mg.apiKey)
                magentoGoAccess.validate(mgc).onComplete(_.fold(
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

                            emailService.sendEmail(
                                pu.email,
                                "Thank you for choosing Echoed",
                                "magentogo_partner_email_register",
                                model)

                            emailService.sendEmail(
                                accountManagerEmail,
                                "New MagentoGo partner %s" format p.name,
                                "magentogo_accountManager_email",
                                model)
                    }))
            } catch {
                case e => error(e)
            }
    }

}
