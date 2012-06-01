package com.echoed.chamber.services.partner.shopify

import reflect.BeanProperty
import akka.actor.{Channel, Actor}
import collection.mutable.ConcurrentMap
import com.echoed.cache.CacheManager
import org.slf4j.LoggerFactory

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


class ShopifyPartnerServiceManagerActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[ShopifyPartnerServiceManagerActor])

    @BeanProperty var shopifyAccess: ShopifyAccess = _
    @BeanProperty var shopifyPartnerDao: ShopifyPartnerDao = _

    @BeanProperty var partnerDao: PartnerDao = _
    @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _
    @BeanProperty var partnerUserDao: PartnerUserDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoMetricsDao: EchoMetricsDao = _
    @BeanProperty var imageDao: ImageDao = _
    @BeanProperty var imageService: ImageService = _
    @BeanProperty var encrypter: Encrypter = _
    @BeanProperty var transactionTemplate: TransactionTemplate = _
    @BeanProperty var emailService: EmailService = _
    @BeanProperty var accountManagerEmail: String = _

    @BeanProperty var accountManagerEmailTemplate = "shopify_partner_accountManager_email"
    @BeanProperty var partnerEmailTemplate = "shopify_partner_email_register"

    @BeanProperty var cacheManager: CacheManager = _

    //represents the parent in Akka 2.0 router setup
    @BeanProperty var partnerServiceManager: PartnerServiceManager = _

    @BeanProperty var properties: Properties = _


    //this will be replaced by the ActorRegistry eventually (I think)
    private var cache: ConcurrentMap[String, PartnerService] = null


    override def preStart() {
        //this is a shared cache with PartnerServiceManagerActor
        cache = cacheManager.getCache[PartnerService]("PartnerServices")

        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        //where placeholder values were not being resolved
        {
            accountManagerEmail = properties.getProperty("accountManagerEmail")
            accountManagerEmail != null
        } ensuring(_ == true, "Missing parameters")
    }


    def receive = {
        case msg @ Locate(partnerId) =>
            implicit val channel: Channel[LocateResponse] = self.channel

            cache.get(partnerId).cata(
                partnerService => {
                    channel ! LocateResponse(msg, Right(partnerService))
                    logger.debug("Cache hit for {}", partnerService)
                },
                Future {
                    val sp = Option(shopifyPartnerDao.findByPartnerId(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    val p = Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    (sp, p)
                }.onComplete(_.value.get.fold(
                    _ match {
                        case e: PartnerNotFound =>
                            logger.debug("Partner not found: {}", partnerId)
                            channel ! LocateResponse(msg, Left(e))
                        case e: PartnerException => channel ! LocateResponse(msg, Left(e))
                        case e => channel ! LocateResponse(msg, Left(PartnerException("Error locating partner %s" format partnerId, e)))
                    },
                    {
                        case (shopifyPartner, partner) =>
                            logger.debug("Found Shopify partner {}", shopifyPartner.name)
                            val partnerService = new ShopifyPartnerServiceActorClient(Actor.actorOf(new ShopifyPartnerServiceActor(
                                shopifyPartner,
                                partner,
                                shopifyAccess,
                                shopifyPartnerDao,
                                partnerDao,
                                partnerSettingsDao,
                                echoDao,
                                echoMetricsDao,
                                imageDao,
                                imageService,
                                transactionTemplate,
                                encrypter)).start())
                            cache.put(partnerId, partnerService)
                            channel ! LocateResponse(msg, Right(partnerService))
                    })))


        case msg @ RegisterShopifyPartner(shop, signature, t, timeStamp) =>
            val channel: Channel[RegisterShopifyPartnerResponse] = self.channel

            def error(e: Throwable) {
                channel ! RegisterShopifyPartnerResponse(msg, Left(ShopifyPartnerException("Could not register Shopify partner", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating Shopfiy partner service for shop {} from token: {}" , shop, t)
                shopifyAccess.fetchShopFromToken(shop, signature, t, timeStamp).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case FetchShopFromTokenResponse(_, Left(e)) => error(e)
                        case FetchShopFromTokenResponse(_, Right(shopifyPartner)) =>
                            partnerServiceManager.locatePartnerByDomain(shopifyPartner.domain).onComplete(_.value.get.fold(
                                error(_),
                                _ match {
                                    case LocateByDomainResponse(_, Left(e: PartnerNotFound)) =>
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

                                    case LocateByDomainResponse(_, Left(e)) => error(e)
                                    case LocateByDomainResponse(_, Right(partnerService)) =>
                                        val sps = partnerService.asInstanceOf[ShopifyPartnerService]
                                        sps.update(shopifyPartner)

                                        val sp = sps.getShopifyPartner
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
                                        }).onComplete(_.value.get.fold(
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
                    }))
            } catch {
                case e =>
                    logger.debug("exception {}" , e)
            }
    }
}
