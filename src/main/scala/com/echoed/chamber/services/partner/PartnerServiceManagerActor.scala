package com.echoed.chamber.services.partner

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
import partner.{PartnerDao, PartnerSettingsDao, PartnerUserDao}
import scala.collection.mutable.ConcurrentMap
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import com.echoed.chamber.services.image.ImageService
import com.echoed.chamber.services.ActorClient
import java.util.{UUID, HashMap => JHashMap}
import org.springframework.beans.factory.FactoryBean
import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.actor.SupervisorStrategy.Restart


class PartnerServiceManagerActor extends FactoryBean[ActorRef] {


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
    @BeanProperty var cacheManager: CacheManager = _


    @BeanProperty var cloudPartners: JHashMap[String, ActorClient] = _

    private var cache: ConcurrentMap[String, PartnerService] = null


    @BeanProperty var actorSystem: ActorSystem = _
    @BeanProperty var timeoutInSeconds = 20


    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    override def preStart() {
        cache = cacheManager.getCache[PartnerService]("PartnerServices", Some(new CacheListenerActorClient(self)))
    }


    def receive = {

        case msg @ CacheEntryRemoved(partnerId: String, partnerService: PartnerService, cause: String) =>
            logger.debug("Received {}", msg)
            partnerService.asInstanceOf[ActorClient].actorRef ! PoisonPill
            logger.debug("Stopped {}", partnerService.id)

        /* Below is an exploratory code block if we were to move away from futures - we would instead match on
           the response message and the original request and sender - this has the downside of breaking up the code
           (can't immediately see how we handle a response like in a future) but the upside of not accidentally
           closing over state
         */
//        case msg @ LocateByDomainResponse(
//                LocateByDomain(domain, Some((RegisterPartner(partner, partnerSettings, partnerUser), channel))),
//                result: Either[PartnerException, PartnerService]) =>


        case msg @ RegisterPartner(partner, partnerSettings, partnerUser) =>
            val me = context.self
            val channel = context.sender


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
                /* Below is an exploratory code block if we were to handle future responses using anonymous actors -
                   this has the downside that we can easily still close over state making it not much different than
                   using onComplete callbacks...
                 */
//                (me ? LocateByDomain(partner.domain)).pipeTo(context.actorOf(Props(new Actor with ActorLogging {
//                    def receive = {
//                        case _ =>
//                    }
//                })))
//
//                me ! LocateByDomain(partner.domain, Some((msg, channel)))
                (me ? LocateByDomain(partner.domain)).onComplete(_.fold(
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

                            (me ? Locate(partner.id)).onComplete(_.fold(
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

        case Create(msg @ Locate(partnerId), channel) =>
            val partnerService = new PartnerServiceActorClient(context.actorOf(Props().withCreator {
                //we do this to force the actor to reload its state from the database or die trying...
                val p = Option(partnerDao.findById(partnerId)).get
                new PartnerServiceActor(
                    p,
                    partnerDao,
                    partnerSettingsDao,
                    echoDao,
                    echoMetricsDao,
                    imageDao,
                    imageService,
                    transactionTemplate,
                    encrypter)
            }, partnerId))
            cache.put(partnerId, partnerService)
            channel ! LocateResponse(msg, Right(partnerService))

        case msg @ Locate(partnerId) =>
            val me = context.self
            val channel = context.sender
            implicit val ec = context.dispatcher

            cache.get(partnerId) match {
                case Some(partnerService) =>
                    channel ! LocateResponse(msg, Right(partnerService))
                    logger.debug("Cache hit for {}", partnerService)
                case _ =>
                    logger.debug("Looking up partner {}", partnerId)
                    Future {
                        Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
                    }.onComplete(_.fold(
                        _ match {
                            case e: PartnerNotFound =>
                                logger.debug("Partner not found {}", partnerId)
                                channel ! LocateResponse(msg, Left(e))
                            case e: PartnerException => channel ! LocateResponse(msg, Left(e))
                            case e => channel ! LocateResponse(msg, Left(PartnerException("Error locating partner %s" format partnerId, e)))
                        },
                        {
                            case partner if (Option(partner.cloudPartnerId) == None) => me ! Create(msg, channel)
                            case partner =>
                                logger.debug("Found {} partner {}", partner.cloudPartnerId, partner.name)
                                cloudPartners.get(partner.cloudPartnerId).actorRef.tell(msg, channel)
                        }))
            }


        case msg @ LocateByEchoId(echoId) =>
            val me = context.self
            val channel = context.sender

            Option(echoDao.findByIdOrPostId(echoId)).cata(
                echo => ((me ? Locate(echo.partnerId)).mapTo[LocateResponse]).onComplete(_.fold(
                    e => channel ! LocateByEchoIdResponse(msg, Left(PartnerException("Unexpected error", e))),
                    _ match {
                        case LocateResponse(_, Left(e)) => channel ! LocateByEchoIdResponse(msg, Left(e))
                        case LocateResponse(_, Right(partnerService)) => channel ! LocateByEchoIdResponse(msg, Right(partnerService))
                    })),
                {
                    channel ! LocateByEchoIdResponse(msg, Left(EchoNotFound(echoId)))
                })


        case msg @ LocateByDomain(domain, _) =>
            val me = context.self
            val channel = context.sender

            Option(partnerDao.findByDomain(domain)).cata(
                partner => (me ? Locate(partner.id)).mapTo[LocateResponse].onComplete(_.fold(
                    e => channel ! LocateByDomainResponse(msg, Left(PartnerException("Unexpected error", e))),
                    _ match {
                        case LocateResponse(_, Left(e)) => channel ! LocateByDomainResponse(msg, Left(e))
                        case LocateResponse(_, Right(partnerService)) => channel ! LocateByDomainResponse(msg, Right(partnerService))
                    })),
                {
                    channel ! LocateByDomainResponse(msg, Left(PartnerNotFound(domain)))
                })


        case msg @ PartnerIdentifiable(partnerId) =>
            val me = context.self
            val channel = context.sender

            val constructor = findResponseConstructor(msg)

            (me ? Locate(partnerId)).mapTo[LocateResponse].onComplete(_.fold(
                e => channel ! constructor.newInstance(msg, Left(new PartnerException("Error locating partner %s" format partnerId, e))),
                _ match {
                    case LocateResponse(Locate(partnerId), Left(e)) => channel ! constructor.newInstance(msg, Left(e))
                    case LocateResponse(_, Right(ps)) => ps.asInstanceOf[ActorClient].actorRef.tell(msg, channel)
                }))


        case msg @ EchoIdentifiable(echoId) =>
            val me = context.self
            val channel = context.sender

            val constructor = findResponseConstructor(msg)

            (me ? LocateByEchoId(echoId)).mapTo[LocateByEchoIdResponse].onComplete(_.fold(
                e => channel ! constructor.newInstance(msg, Left(new PartnerException("Error locating with echo id %s" format echoId, e))),
                _ match {
                    case LocateByEchoIdResponse(_, Left(e)) => channel ! constructor.newInstance(msg, Left(e))
                    case LocateByEchoIdResponse(_, Right(ps)) => ps.asInstanceOf[ActorClient].actorRef.tell(msg, channel)
                }))
    }


    private def findResponseConstructor(msg: PartnerMessage) = {
        val requestClass = msg.getClass
        val responseClass = Thread.currentThread.getContextClassLoader.loadClass(requestClass.getName + "Response")
        responseClass.getConstructor(requestClass, classOf[Either[_, _]])
    }

    }), "PartnerServiceManager")
}



