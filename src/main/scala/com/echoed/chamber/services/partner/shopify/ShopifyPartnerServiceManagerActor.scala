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


class ShopifyPartnerServiceManagerActor extends FactoryBean[ActorRef] {


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


    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    private implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

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

        case Create(msg @ Locate(partnerId), channel) =>
            val partnerService = cache.get(partnerId).getOrElse {
                val ps = new ShopifyPartnerServiceActorClient(context.actorOf(Props().withCreator {
                    val sp = Option(shopifyPartnerDao.findByPartnerId(partnerId)).get
                    val p = Option(partnerDao.findById(partnerId)).get
                    logger.debug("Found Shopify partner {}", sp.name)
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
                    logger.debug("Cache hit for {}", partnerService)
                },
                Future {
                    val sp = Option(shopifyPartnerDao.findByPartnerId(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    val p = Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    (sp, p)
                }.onComplete(_.fold(
                    _ match {
                        case e: PartnerNotFound =>
                            logger.debug("Partner not found: {}", partnerId)
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
                logger.error("Error processing {}: {}", msg, e)
            }

            try {
                logger.debug("Creating Shopfiy partner service for shop {} from token: {}" , shop, t)
                shopifyAccess.fetchShopFromToken(shop, signature, t, timeStamp).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchShopFromTokenResponse(_, Left(e)) => error(e)
                        case FetchShopFromTokenResponse(_, Right(shopifyPartner)) =>
                            partnerServiceManager.locatePartnerByDomain(shopifyPartner.domain).onComplete(_.fold(
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

                                        (me ? Locate(p.id)).onComplete(_.fold(
                                            e => logger.error("Unexpected error creating Shopify partner after registration: {}", e),
                                            _ match {
                                                case LocateResponse(_, Left(e)) =>
                                                    logger.error("Unexpected error creating Shopify partner after registration: {}", e)
                                                case LocateResponse(_, Right(_)) =>
                                                    logger.debug("Successfully registered and created Shopify partner: {}", sp.name)
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

                                    case LocateByDomainResponse(_, Left(e)) => error(e)
                                    case LocateByDomainResponse(_, Right(partnerService)) =>
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
                    }))
            } catch {
                case e => logger.error("Unexpected exception during registration of ShopifyPartner {}" , e)
            }
    }

    }), "ShopifyPartnerServiceManager")
}
