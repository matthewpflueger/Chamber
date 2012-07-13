package com.echoed.chamber.services.partner.bigcommerce

import collection.JavaConversions._
import collection.mutable.ConcurrentMap
import com.echoed.cache.CacheManager
import com.echoed.chamber.services.partner._
import org.springframework.transaction.TransactionStatus
import com.echoed.chamber.dao._
import com.echoed.chamber.services.image.ImageService
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.email.EmailService
import com.echoed.util.TransactionUtils._
import partner.bigcommerce.BigCommercePartnerDao
import partner.{PartnerDao, PartnerUserDao, PartnerSettingsDao}
import scalaz._
import Scalaz._
import java.util.{HashMap, UUID}
import com.echoed.chamber.domain.partner.{PartnerUser, Partner, PartnerSettings}
import com.echoed.util.{ObjectUtils, Encrypter}
import com.echoed.chamber.domain.partner.bigcommerce.BigCommerceCredentials
import akka.actor._
import akka.util.duration._
import akka.actor.SupervisorStrategy.Restart
import com.echoed.chamber.services.EchoedActor


class BigCommercePartnerServiceManagerActor(
        bigCommerceAccess: BigCommerceAccess,
        bigCommercePartnerDao: BigCommercePartnerDao,
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
        cacheManager: CacheManager) extends EchoedActor {

    //this will be replaced by the ActorRegistry eventually (I think)
    private var cache: ConcurrentMap[String, PartnerService] = cacheManager.getCache[PartnerService]("PartnerServices")

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }


    def handle = {
        case Create(msg @ Locate(partnerId), channel) =>
            val partnerService = cache.get(partnerId).getOrElse {
                val ps = new BigCommercePartnerServiceActorClient(context.actorOf(Props().withCreator {
                    val bcp = Option(bigCommercePartnerDao.findByPartnerId(partnerId)).get
                    val p = Option(partnerDao.findById(partnerId)).get
                    log.debug("Found BigCommerce partner {}", bcp.name)
                    new BigCommercePartnerServiceActor(
                        bcp,
                        p,
                        bigCommerceAccess,
                        bigCommercePartnerDao,
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
                    Left(PartnerException("Could not locate BigCommerce partner", e)))
                    log.error("Error processing {}: {}", msg, e)
            }

            try {
                cache.get(partnerId).cata(
                    partnerService => {
                        channel ! LocateResponse(msg, Right(partnerService))
                        log.debug("Cache hit for {}", partnerService)
                    },
                    {
                        Option(bigCommercePartnerDao.findByPartnerId(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                        Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                        me ! Create(msg, channel)
                    })
            } catch {
                case e => error(e)
            }

        case msg @ RegisterBigCommercePartner(bc) =>
            val channel = context.sender

            def error(e: Throwable) = {
                e match {
                    case pe: PartnerException => channel ! RegisterBigCommercePartnerResponse(msg, Left(pe))
                    case _ => channel ! RegisterBigCommercePartnerResponse(
                        msg,
                        Left(BigCommercePartnerException("Could not register BigCommerce partner", e)))
                        log.error("Error processing {}: {}", msg, e)
                }

                emailService.sendEmail(
                    accountManagerEmail,
                    "Failed BigCommerce partner %s" format bc.businessName,
                    "bigcommerce_accountManager_register_email",
                    ObjectUtils.asMap(bc))
            }

            try {
                val bcc = BigCommerceCredentials(bc.apiPath, bc.apiUser, bc.apiToken)
                bigCommerceAccess.validate(bcc).onComplete(_.fold(
//                (bigCommerceAccess ? Validate(bcc)).onComplete(_.fold(
                    error(_),
                    _ match {
                        case ValidateResponse(_, Left(e)) => error(e)
                        case ValidateResponse(_, Right(false)) =>
                            error(BigCommercePartnerException("Could not connect to BigCommerce api"))
                        case ValidateResponse(_, Right(true)) =>
                            val p = new Partner(
                                        name = bc.businessName,
                                        domain = bc.storeUrl,
                                        phone = bc.phone,
                                        logo = null,
                                        category = "Other",
                                        handle = null).copy(cloudPartnerId = "BigCommerce")
                            val bcp = bc.copy(partnerId = p.id)

                            val password = UUID.randomUUID().toString
                            val pu = new PartnerUser(p.id, bc.name, bc.email).createPassword(password)
                            val ps = PartnerSettings.createPartnerSettings(p.id)
                            val code = encrypter.encrypt("""{"email": "%s", "password": "%s"}""" format(pu.email, password))

                            log.debug("Creating BigCommerce partner service for {}, {}", p.name, pu.email)
                            transactionTemplate.execute({ status: TransactionStatus =>
                                partnerDao.insert(p)
                                partnerSettingsDao.insert(ps)
                                partnerUserDao.insert(pu)
                                bigCommercePartnerDao.insert(bcp)
                            })

                            channel ! RegisterBigCommercePartnerResponse(
                                msg,
                                Right(RegisterBigCommercePartnerEnvelope(bcp, p, pu)))


                            val model = new HashMap[String, AnyRef]()
                            model.put("code", code)
                            model.put("bigCommercePartner", bcp)
                            model.put("partner", p)
                            model.put("partnerUser", pu)

                            emailService.sendEmail(
                                pu.email,
                                "Thank you for choosing Echoed",
                                "bigcommerce_partner_email_register",
                                model)

                            emailService.sendEmail(
                                accountManagerEmail,
                                "New BigCommerce partner %s" format p.name,
                                "bigcommerce_accountManager_email",
                                model)
                    }))
            } catch {
                case e => error(e)
            }
    }


}
