package com.echoed.chamber.services.partner

import org.slf4j.LoggerFactory
import akka.actor.{Channel, Actor}
import scala.reflect.BeanProperty
import com.echoed.util.Encrypter
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.{TransactionCallback, TransactionTemplate}
import com.echoed.chamber.services.email.EmailService
import com.echoed.util.TransactionUtils._
import scalaz._
import Scalaz._
import akka.dispatch.Future
import java.util.{Date, UUID, HashMap => JHashMap}
import com.echoed.chamber.dao._
import scala.collection.mutable.ConcurrentMap
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import com.echoed.chamber.services.ActorClient


class PartnerServiceManagerActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerServiceManagerActor])

    @BeanProperty var partnerDao: RetailerDao = _
    @BeanProperty var partnerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var partnerUserDao: RetailerUserDao = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var encrypter: Encrypter = _
    @BeanProperty var transactionTemplate: TransactionTemplate = _
    @BeanProperty var emailService: EmailService = _

    @BeanProperty var cacheManager: CacheManager = _


    private var cache: ConcurrentMap[String, PartnerService] = null


    override def preStart() {
        cache = cacheManager.getCache[PartnerService]("PartnerServices", Some(new CacheListenerActorClient(self)))
    }


    def receive = {

        case msg @ CacheEntryRemoved(partnerId: String, partnerService: PartnerService, cause: String) =>
            logger.debug("Received {}", msg)
            partnerService.asInstanceOf[ActorClient].actorRef.stop()
            logger.debug("Stopped {}", partnerService.id)

        case msg @ RegisterPartner(partner, partnerSettings, partnerUser) =>
            val me = self
            val channel: Channel[RegisterPartnerResponse] = self.channel


            def error(e: Throwable) {
                logger.error("Unexpected error processing %s" format msg, e)
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
                val p = partner.copy(secret = encrypter.generateSecretKey)
                val ps = partnerSettings.copy(retailerId = p.id)
                val password = UUID.randomUUID().toString
                val pu = partnerUser.copy(retailerId = p.id).createPassword(password)
                val code = encrypter.encrypt("""{"email": "%s", "password": "%s"}""" format (partnerUser.email, password))


                transactionTemplate.execute({status: TransactionStatus =>
                        partnerDao.insert(p)
                        partnerSettingsDao.insert(ps)
                        partnerUserDao.insert(pu)
                })

                channel ! RegisterPartnerResponse(msg, Right(p))
                me ! Locate(p.id)

                val model = new JHashMap[String, AnyRef]()
                model.put("code", code)
                model.put("partner", p)
                model.put("partnerUser", pu)
                emailService.sendEmail(
                    partnerUser.email,
                    "Your Echoed Account",
                    "partner_email_register",
                    model)

            } catch {
                case e => error(e)
            }


        case msg @ Locate(partnerId) =>
            val channel: Channel[LocateResponse] = self.channel

            cache.get(partnerId) match {
                case Some(partnerService) =>
                    channel ! LocateResponse(msg, Right(partnerService))
                    logger.debug("Cache hit for {}", partnerService)
                case _ =>
                    val pf = Future {
                        Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    }
                    val psf = Future {
                        Option(partnerSettingsDao.findByActiveOn(partnerId, new Date()))
                                .getOrElse(throw PartnerNotActive(partnerId))
                    }

                    (for {
                        partner <- pf
                        partnerSettings <- psf
                    } yield {
                        val partnerService = new PartnerServiceActorClient(Actor.actorOf(new PartnerServiceActor(
                                partner,
                                partnerSettings,
                                partnerDao,
                                partnerSettingsDao,
                                echoPossibilityDao,
                                echoDao,
                                encrypter)).start)
                        channel ! LocateResponse(msg, Right(partnerService))
                        cache.put(partnerId, partnerService)
                    }).onException {
                        case e: PartnerException => channel ! LocateResponse(msg, Left(e))
                        case e => channel ! LocateResponse(msg, Left(PartnerException("Error locating partner %s" format partnerId, e)))
                    }
            }

        case msg @ LocateByEchoId(echoId) =>
            val me = self
            val channel: Channel[LocateByEchoIdResponse] = self.channel

//            Option(echoDao.findById(echoId)).cata(
            Option(echoPossibilityDao.findById(echoId)).cata(
                echo => ((me ? Locate(echo.retailerId)).mapTo[LocateResponse]).onComplete(_.value.get.fold(
                    e => channel ! LocateByEchoIdResponse(msg, Left(PartnerException("Unexpected error", e))),
                    _ match {
                        case LocateResponse(_, Left(e)) => channel ! LocateByEchoIdResponse(msg, Left(e))
                        case LocateResponse(_, Right(partnerService)) => channel ! LocateByEchoIdResponse(msg, Right(partnerService))
                    })),
                {
                    channel ! LocateByEchoIdResponse(msg, Left(EchoNotFound(echoId)))
                })

    }

}
