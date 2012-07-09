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


class NetworkSolutionsPartnerServiceManagerActor(
        networkSolutionsAccess: NetworkSolutionsAccess,
        networkSolutionsPartnerDao: NetworkSolutionsPartnerDao,
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
        accountManagerRegisterEmailTemplate: String = "networksolutions_accountManager_register_email",
        accountManagerEmailTemplate: String = "networksolutions_accountManager_email",
        partnerEmailTemplate: String = "networksolutions_partner_email_register",
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
                val ps = new NetworkSolutionsPartnerServiceActorClient(context.actorOf(Props().withCreator {
                    val nsp = Option(networkSolutionsPartnerDao.findByPartnerId(partnerId)).get
                    val p = Option(partnerDao.findById(partnerId)).get
                    log.debug("Found NetworkSolutions partner {}", nsp.name)
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
                    log.debug("Cache hit for {}", partnerService)
                },
                Future {
                    val nsp = Option(networkSolutionsPartnerDao.findByPartnerId(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    val p = Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    (nsp, p)
                }.onComplete(_.fold(
                    _ match {
                        case e: PartnerNotFound =>
                            log.debug("Partner not found: {}", partnerId)
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
                    log.error("Error processing {}: {}", msg, e)
            }

            try {
                log.debug("Creating Network Solutions partner service for {}, {}", name, email)
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
                    log.error("Error processing {}: {}", msg, e)
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
                        log.error("Did not find a new Network Solutions partner with user key {}", userKey)
                    }
                )
            } catch {
                case e => error(e)
            }
    }

}
