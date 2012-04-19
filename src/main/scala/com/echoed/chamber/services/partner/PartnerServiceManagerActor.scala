package com.echoed.chamber.services.partner

import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import com.echoed.util.Encrypter
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.email.EmailService
import com.echoed.util.TransactionUtils._
import scalaz._
import Scalaz._
import akka.dispatch.Future
import com.echoed.chamber.dao._
import scala.collection.mutable.ConcurrentMap
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import com.echoed.chamber.services.image.ImageService
import com.echoed.chamber.services.ActorClient
import akka.actor.{PoisonPill, Channel, Actor}
import java.util.{UUID, HashMap => JHashMap}


class PartnerServiceManagerActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerServiceManagerActor])

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

    @BeanProperty var cloudPartners: JHashMap[String, ActorClient] = _

    @BeanProperty var cacheManager: CacheManager = _


    private var cache: ConcurrentMap[String, PartnerService] = null


    override def preStart() {
        cache = cacheManager.getCache[PartnerService]("PartnerServices", Some(new CacheListenerActorClient(self)))
    }


    def receive = {

        case msg @ CacheEntryRemoved(partnerId: String, partnerService: PartnerService, cause: String) =>
            logger.debug("Received {}", msg)
            partnerService.asInstanceOf[ActorClient].actorRef tryTell PoisonPill
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
                (me ? LocateByDomain(partner.domain)).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case LocateByDomainResponse(_, Right(partnerService)) =>
                            logger.debug("Partner already exists {}", partner.domain)
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

                            emailService.sendEmail(
                                    partnerUser.email,
                                    "Your Echoed Account",
                                    "partner_email_register",
                                    model)

                            (me ? Locate(partner.id)).onComplete(_.value.get.fold(
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


        case msg @ Locate(partnerId) =>
            implicit val channel: Channel[LocateResponse] = self.channel

            cache.get(partnerId) match {
                case Some(partnerService) =>
                    channel ! LocateResponse(msg, Right(partnerService))
                    logger.debug("Cache hit for {}", partnerService)
                case _ =>
                    logger.debug("Looking up partner {}", partnerId)
                    Future {
                        Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    }.onComplete(_.value.get.fold(
                        _ match {
                            case e: PartnerNotFound =>
                                logger.debug("Partner not found {}", partnerId)
                                channel ! LocateResponse(msg, Left(e))
                            case e: PartnerException => channel ! LocateResponse(msg, Left(e))
                            case e => channel ! LocateResponse(msg, Left(PartnerException("Error locating partner %s" format partnerId, e)))
                        },
                        {
                            case partner if (Option(partner.cloudPartnerId) == None) =>
                                val partnerService = new PartnerServiceActorClient(Actor.actorOf(new PartnerServiceActor(
                                    partner,
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

                            case partner =>
                                logger.debug("Found {} partner {}", partner.cloudPartnerId, partner.name)
                                cloudPartners.get(partner.cloudPartnerId).actorRef forward msg
                        }))
            }


        case msg @ LocateByEchoId(echoId) =>
            val me = self
            val channel: Channel[LocateByEchoIdResponse] = self.channel

            Option(echoDao.findById(echoId)).cata(
                echo => ((me ? Locate(echo.partnerId)).mapTo[LocateResponse]).onComplete(_.value.get.fold(
                    e => channel ! LocateByEchoIdResponse(msg, Left(PartnerException("Unexpected error", e))),
                    _ match {
                        case LocateResponse(_, Left(e)) => channel ! LocateByEchoIdResponse(msg, Left(e))
                        case LocateResponse(_, Right(partnerService)) => channel ! LocateByEchoIdResponse(msg, Right(partnerService))
                    })),
                {
                    channel ! LocateByEchoIdResponse(msg, Left(EchoNotFound(echoId)))
                })


        case msg @ LocateByDomain(domain) =>
            val me = self
            val channel: Channel[LocateByDomainResponse] = self.channel

            Option(partnerDao.findByDomain(domain)).cata(
                partner => (me ? Locate(partner.id)).mapTo[LocateResponse].onComplete(_.value.get.fold(
                    e => channel ! LocateByDomainResponse(msg, Left(PartnerException("Unexpected error", e))),
                    _ match {
                        case LocateResponse(_, Left(e)) => channel ! LocateByDomainResponse(msg, Left(e))
                        case LocateResponse(_, Right(partnerService)) => channel ! LocateByDomainResponse(msg, Right(partnerService))
                    })),
                {
                    channel ! LocateByDomainResponse(msg, Left(PartnerNotFound(domain)))
                })


        case msg @ PartnerIdentifiable(partnerId) =>
            val me = self
            val channel = self.channel

            val constructor = findResponseConstructor(msg)

            (me ? Locate(partnerId)).mapTo[LocateResponse].onComplete(_.value.get.fold(
                e => channel ! constructor.newInstance(msg, Left(new PartnerException("Error locating partner %s" format partnerId, e))),
                _ match {
                    case LocateResponse(_, Left(e)) => channel ! constructor.newInstance(msg, Left(e))
                    case LocateResponse(_, Right(ps)) => ps.asInstanceOf[ActorClient].actorRef forward msg
                }))


        case msg @ EchoIdentifiable(echoId) =>
            val me = self
            val channel = self.channel

            val constructor = findResponseConstructor(msg)

            (me ? LocateByEchoId(echoId)).mapTo[LocateByEchoIdResponse].onComplete(_.value.get.fold(
                e => channel ! constructor.newInstance(msg, Left(new PartnerException("Error locating with echo id %s" format echoId, e))),
                _ match {
                    case LocateByEchoIdResponse(_, Left(e)) => channel ! constructor.newInstance(msg, Left(e))
                    case LocateByEchoIdResponse(_, Right(ps)) => ps.asInstanceOf[ActorClient].actorRef forward msg
                }))
    }


    private def findResponseConstructor(msg: PartnerMessage) = {
        val requestClass = msg.getClass
        val responseClass = Thread.currentThread.getContextClassLoader.loadClass(requestClass.getName + "Response")
        responseClass.getConstructor(requestClass, classOf[Either[_, _]])
    }
}



