package com.echoed.chamber.services.partner.shopify

import reflect.BeanProperty
import collection.mutable.ConcurrentMap
import com.echoed.cache.CacheManager


import com.echoed.chamber.services.partner._
import org.springframework.transaction.TransactionStatus
import com.echoed.util.Encrypter
import com.echoed.chamber.dao._
import com.echoed.chamber.services.image.ImageService
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.email.EmailService
import com.echoed.util.TransactionUtils._
import partner.shopify.ShopifyPartnerDao
import partner.{PartnerDao, PartnerSettingsDao, PartnerUserDao}
import scalaz._
import Scalaz._
import akka.dispatch.Future
import java.util.{Properties, HashMap, UUID}
import com.echoed.chamber.domain.partner.shopify.ShopifyPartner
import com.echoed.chamber.domain.partner.PartnerSettings
import org.springframework.beans.factory.FactoryBean
import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import akka.util.duration._
import akka.event.Logging
import akka.actor.SupervisorStrategy.Restart


class ShopifyPartnerServiceManagerActor(
        shopifyAccess: ShopifyAccess,
        shopifyPartnerDao: ShopifyPartnerDao,
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
        accountManagerEmailTemplate: String = "shopify_partner_accountManager_email",
        partnerEmailTemplate: String = "shopify_partner_email_register",
        cacheManager: CacheManager,
        partnerServiceManager: PartnerServiceManager,
        implicit val timeout: Timeout = Timeout(20000)) extends Actor with ActorLogging {

    //this will be replaced by the ActorRegistry eventually (I think)
    private val cache = cacheManager.getCache[PartnerService]("PartnerServices")

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    def receive = {

        case Create(msg @ Locate(partnerId), channel) =>
            val partnerService = cache.get(partnerId).getOrElse {
                val ps = new ShopifyPartnerServiceActorClient(context.actorOf(Props().withCreator {
                    val sp = Option(shopifyPartnerDao.findByPartnerId(partnerId)).get
                    val p = Option(partnerDao.findById(partnerId)).get
                    log.debug("Found Shopify partner {}", sp.name)
                    new ShopifyPartnerServiceActor(
                        sp,
                        p,
                        shopifyAccess,
                        shopifyPartnerDao,
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
            implicit val ec = context.dispatcher

            cache.get(partnerId).cata(
                partnerService => {
                    channel ! LocateResponse(msg, Right(partnerService))
                    log.debug("Cache hit for {}", partnerService)
                },
                Future {
                    val sp = Option(shopifyPartnerDao.findByPartnerId(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    val p = Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    (sp, p)
                }.onComplete(_.fold(
                    _ match {
                        case e: PartnerNotFound =>
                            log.debug("Partner not found: {}", partnerId)
                            channel ! LocateResponse(msg, Left(e))
                        case e: PartnerException => channel ! LocateResponse(msg, Left(e))
                        case e => channel ! LocateResponse(msg, Left(PartnerException("Error locating partner %s" format partnerId, e)))
                    },
                    {
                        case (shopifyPartner, partner) => me ! Create(msg, channel)
                    })))


        case msg @ RegisterShopifyPartner(shop, signature, t, timeStamp) =>
            val me = context.self
            val channel = context.sender
            implicit val ec = context.dispatcher

            def error(e: Throwable) {
                channel ! RegisterShopifyPartnerResponse(msg, Left(ShopifyPartnerException("Could not register Shopify partner", e)))
                log.error("Error processing {}: {}", msg, e)
            }

            try {
                log.debug("Creating Shopfiy partner service for shop {} from token: {}" , shop, t)
                shopifyAccess.fetchShopFromToken(shop, signature, t, timeStamp).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchShopFromTokenResponse(_, Left(e)) => error(e)
                        case FetchShopFromTokenResponse(_, Right(shopifyPartner)) =>
                            Option(shopifyPartnerDao.findByShopifyId(shopifyPartner.shopifyId)).cata(sp => {
                                (me ? Locate(sp.partnerId)).onComplete(_.fold(
                                    e => log.error("Unexpected error fetching Shopify partner: {}", e),
                                    _ match {
                                        case LocateResponse(_, Left(e)) => log.error("Error fetching Shopify partner: {}", e)
                                        case LocateResponse(_, Right(partnerService)) =>
                                            log.debug("Successfully fetched Shopify partner: {}", shopifyPartner.name)

                                            val sps = partnerService.asInstanceOf[ShopifyPartnerService]
                                            sps.update(shopifyPartner)

                                            val sp = sps.getShopifyPartner
                                            implicit val t = Timeout(20 seconds)
                                            val pu = Future { partnerUserDao.findByEmail(shopifyPartner.email) }

                                            (for {
                                                sp <- sp
                                                pu <- pu
                                            } yield {
                                                RegisterShopifyPartnerEnvelope(
                                                    sp.resultOrException,
                                                    Option(partnerDao.findById(sp.resultOrException.partnerId)).get,
                                                    pu,
                                                    Some(sps))
                                            }).onComplete(_.fold(
                                                _ match {
                                                    case e: PartnerException => channel ! RegisterShopifyPartnerResponse(msg, Left(e))
                                                    case e => channel ! RegisterShopifyPartnerResponse(
                                                                    msg,
                                                                    Left(PartnerAlreadyExists(partnerService, _cause = e)))
                                                },
                                                envelope => channel ! RegisterShopifyPartnerResponse(
                                                                    msg,
                                                                    Left(ShopifyPartnerAlreadyExists(envelope)))
                                            ))
                                    }))
                                },
                                {
                                    val (p, sp) = ShopifyPartner.createPartner(shopifyPartner)
                                    val password = UUID.randomUUID().toString
                                    val pu = ShopifyPartner.createPartnerUser(sp).createPassword(password)
                                    val ps = PartnerSettings.createPartnerSettings(p.id)

                                    val code = encrypter.encrypt("""{"email": "%s", "password": "%s"}""" format (pu.email, password))

                                    transactionTemplate.execute({status: TransactionStatus =>
                                        partnerDao.insert(p)
                                        partnerSettingsDao.insert(ps)
                                        partnerUserDao.insert(pu)
                                        shopifyPartnerDao.insert(sp)
                                    })


                                    channel ! RegisterShopifyPartnerResponse(
                                        msg,
                                        Right(RegisterShopifyPartnerEnvelope(sp, p, pu)))

                                    (me ? Locate(p.id)).onComplete(_.fold(
                                        e => log.error("Unexpected error creating Shopify partner after registration: {}", e),
                                        _ match {
                                            case LocateResponse(_, Left(e)) =>
                                                log.error("Unexpected error creating Shopify partner after registration: {}", e)
                                            case LocateResponse(_, Right(_)) =>
                                                log.debug("Successfully registered and created Shopify partner: {}", sp.name)
                                        }))

                                    val model = new HashMap[String, AnyRef]()
                                    model.put("code", code)
                                    model.put("shopifyPartner", sp)
                                    model.put("partnerUser", pu)
                                    model.put("partner", p)

                                    emailService.sendEmail(
                                        pu.email,
                                        "Thank you for choosing Echoed",
                                        partnerEmailTemplate,
                                        model)

                                    emailService.sendEmail(
                                        accountManagerEmail,
                                        "New Shopify partner %s" format sp.name,
                                        accountManagerEmailTemplate,
                                        model)
                                })
                    }))
            } catch {
                case e => log.error("Unexpected exception during registration of ShopifyPartner {}" , e)
            }
    }

}
