package com.echoed.chamber.services.partner.networksolutions

import reflect.BeanProperty
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
import partner.networksolutions.NetworkSolutionsPartnerDao
import partner.{PartnerSettingsDao, PartnerUserDao, PartnerDao}
import scalaz._
import Scalaz._
import akka.dispatch.Future
import java.util.{Properties, HashMap, UUID}
import com.echoed.chamber.domain.partner.networksolutions.NetworkSolutionsPartner
import com.echoed.chamber.domain.partner.{PartnerSettings, PartnerUser, Partner}
import org.springframework.beans.factory.FactoryBean
import akka.actor._
import akka.util.Timeout
import akka.util.duration._
import akka.event.Logging
import akka.actor.SupervisorStrategy.Restart


class NetworkSolutionsPartnerServiceManagerActor extends FactoryBean[ActorRef] {

    @BeanProperty var networkSolutionsAccess: NetworkSolutionsAccess = _
    @BeanProperty var networkSolutionsPartnerDao: NetworkSolutionsPartnerDao = _

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


    @BeanProperty var accountManagerRegisterEmailTemplate = "networksolutions_accountManager_register_email"
    @BeanProperty var accountManagerEmailTemplate = "networksolutions_accountManager_email"
    @BeanProperty var partnerEmailTemplate = "networksolutions_partner_email_register"

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

    implicit val timeout = Timeout(timeoutInSeconds seconds)
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
                val ps = new NetworkSolutionsPartnerServiceActorClient(context.actorOf(Props().withCreator {
                    val nsp = Option(networkSolutionsPartnerDao.findByPartnerId(partnerId)).get
                    val p = Option(partnerDao.findById(partnerId)).get
                    logger.debug("Found NetworkSolutions partner {}", nsp.name)
                    new NetworkSolutionsPartnerServiceActor(
                        nsp,
                        p,
                        networkSolutionsAccess,
                        networkSolutionsPartnerDao,
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
                    val nsp = Option(networkSolutionsPartnerDao.findByPartnerId(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    val p = Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    (nsp, p)
                }.onComplete(_.fold(
                    _ match {
                        case e: PartnerNotFound =>
                            logger.debug("Partner not found: {}", partnerId)
                            channel ! LocateResponse(msg, Left(e))
                        case e: PartnerException => channel ! LocateResponse(msg, Left(e))
                        case e => channel ! LocateResponse(msg, Left(PartnerException("Error locating partner %s" format partnerId, e)))
                    },
                    {
                        case (networkSolutionsPartner, partner) => me ! Create(msg, channel)
                    })))


        case msg @ RegisterNetworkSolutionsPartner(name, email, phone, successUrl, failureUrl) =>
            val channel = context.sender

            def error(e: Throwable) = e match {
                case pe: PartnerException => channel ! RegisterNetworkSolutionsPartnerResponse(msg, Left(pe))
                case _ => channel ! RegisterNetworkSolutionsPartnerResponse(
                    msg,
                    Left(NetworkSolutionsPartnerException("Could not register Network Solutions partner", e)))
                    logger.error("Error processing {}: {}", msg, e)
            }

            try {
                logger.debug("Creating Network Solutions partner service for {}, {}", name, email)
                networkSolutionsAccess.fetchUserKey(successUrl, failureUrl).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchUserKeyResponse(_, Left(e)) => error(e)
                        case FetchUserKeyResponse(_, Right(FetchUserKeyEnvelope(loginUrl, userKey))) =>
                            networkSolutionsPartnerDao.insert(new NetworkSolutionsPartner(name, email, phone, userKey))
                            channel ! RegisterNetworkSolutionsPartnerResponse(msg, Right(loginUrl))

                            val model = new HashMap[String, AnyRef]()
                            model.put("name", name)
                            model.put("email", email)
                            model.put("phone", phone)

                            emailService.sendEmail(
                                email,
                                "%s has registered their Network Solutions store" format name,
                                accountManagerRegisterEmailTemplate,
                                model)
                    }))
            } catch {
                case e => error(e)
            }

        case msg @ AuthNetworkSolutionsPartner(userKey) =>
            val channel = context.sender

            def error(e: Throwable) = e match {
                case pe: PartnerException => channel ! AuthNetworkSolutionsPartnerResponse(msg, Left(pe))
                case _ => channel ! AuthNetworkSolutionsPartnerResponse(
                    msg,
                    Left(NetworkSolutionsPartnerException("Could not authorize Network Solutions partner", e)))
                    logger.error("Error processing {}: {}", msg, e)
            }

            try {
                Option(networkSolutionsPartnerDao.findByUserKey(userKey)).cata(
                    ns => {
                        networkSolutionsAccess.fetchUserToken(userKey).onComplete(_.fold(
                            error(_),
                            _ match {
                                case FetchUserTokenResponse(_, Left(e)) => error(e)
                                case FetchUserTokenResponse(_, Right(FetchUserTokenEnvelope(
                                        userToken, expiresOn, companyName, storeUrl, secureStoreUrl))) =>

                                        val p = new Partner(
                                                name = companyName,
                                                domain = storeUrl,
                                                phone = ns.phone,
                                                handle = null,
                                                logo = null,
                                                category = "Other").copy(cloudPartnerId = "Network Solutions")

                                        val nsp = ns.copy(
                                                userToken = userToken,
                                                userTokenExpiresOn = expiresOn,
                                                companyName = companyName,
                                                storeUrl = storeUrl,
                                                secureStoreUrl = secureStoreUrl,
                                                partnerId = p.id)


                                        val password = UUID.randomUUID().toString
                                        val pu = new PartnerUser(p.id, nsp.name, nsp.email).createPassword(password)
                                        val ps = PartnerSettings.createPartnerSettings(p.id)

                                        val code = encrypter.encrypt("""{"email": "%s", "password": "%s"}""" format (pu.email, password))

                                        transactionTemplate.execute({status: TransactionStatus =>
                                            partnerDao.insert(p)
                                            partnerSettingsDao.insert(ps)
                                            partnerUserDao.insert(pu)
                                            networkSolutionsPartnerDao.update(nsp)
                                        })

                                        channel ! AuthNetworkSolutionsPartnerResponse(
                                            msg,
                                            Right(NetworkSolutionsPartnerEnvelope(nsp, p, pu)))

                                        val model = new HashMap[String, AnyRef]()
                                        model.put("code", code)
                                        model.put("networkSolutionsPartner", nsp)
                                        model.put("partnerUser", pu)
                                        model.put("partner", p)

                                        emailService.sendEmail(
                                            pu.email,
                                            "Thank you for choosing Echoed",
                                            partnerEmailTemplate,
                                            model)

                                        emailService.sendEmail(
                                            accountManagerEmail,
                                            "New Network Solutions partner %s" format p.name,
                                            accountManagerEmailTemplate,
                                            model)

                            }
                        ))
                    },
                    {
                        channel ! AuthNetworkSolutionsPartnerResponse(msg, Left(PartnerNotFound(userKey)))
                        logger.error("Did not find a new Network Solutions partner with user key {}", userKey)
                    }
                )
            } catch {
                case e => error(e)
            }
    }

    }), "NetworkSolutionsPartnerServiceManager")
}
