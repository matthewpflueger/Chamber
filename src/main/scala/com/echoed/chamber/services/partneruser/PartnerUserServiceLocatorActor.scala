package com.echoed.chamber.services.partneruser

import reflect.BeanProperty
import scalaz._
import Scalaz._
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import akka.actor._
import scala.collection.mutable.ConcurrentMap
import com.echoed.util.Encrypter
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import com.echoed.chamber.dao.partner.{PartnerUserDao, PartnerDao}
import com.echoed.chamber.dao.views.PartnerViewDao
import akka.util.Timeout
import akka.util.duration._
import akka.event.Logging
import akka.actor.SupervisorStrategy.Restart
import akka.pattern.ask
import org.springframework.beans.factory.FactoryBean


class PartnerUserServiceLocatorActor extends FactoryBean[ActorRef] {

    @BeanProperty var cacheManager: CacheManager = _
    @BeanProperty var encrypter: Encrypter = _
    @BeanProperty var partnerDao: PartnerDao = _
    @BeanProperty var partnerUserDao: PartnerUserDao = _
    @BeanProperty var partnerViewDao: PartnerViewDao = _

    private val cache: ConcurrentMap[String, PartnerUserService] = new ConcurrentHashMap[String, PartnerUserService]()
    private var cacheById: ConcurrentMap[String, PartnerUserService] = null

    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private val logger = Logging(context.system, this)

    override def preStart() {
        cacheById = cacheManager.getCache[PartnerUserService](
                "PartnerUserServices",
                Some(new CacheListenerActorClient(self)))
    }

    def login(msg: Login, channel: ActorRef) {
        val email = msg.email
        val password = msg.password
        logger.debug("Locating PartnerService for {}", email)

        cache.get(email) match {
            case Some(partnerUserService) =>
                logger.debug("Cache hit for {}", email)
                partnerUserService.getPartnerUser.onSuccess {
                    case GetPartnerUserResponse(_, Right(pu)) =>
                        if (pu.isPassword(password)) {
                            cacheById.put(pu.id, partnerUserService)
                            channel ! LoginResponse(msg, Right(partnerUserService))
                            logger.debug("Valid login for {}", email)
                        } else {
                            channel ! LoginResponse(msg, Left(LoginError("Invalid login")))
                            logger.debug("Invalid login for {}", email)
                        }
                    case GetPartnerUserResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        logger.error("Error getting PartnerUser from PartneruserService for {}", email)
                }

            case None =>
                logger.debug("Cache miss for {}", email)
                (self ? CreatePartnerUserService(email)).onSuccess {
                    case CreatePartnerUserServiceResponse(_, Right(pus)) =>
                        logger.debug("Seeded cache for {}", email)
                        login(msg, channel)
                    case CreatePartnerUserServiceResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        logger.debug("Invalid login for {}", email)
                }
        }
    }


    def receive = {

        case msg @ CreatePartnerUserService(email) =>
            val channel = context.sender

            logger.debug("Loading PartnerUser for {}", email)
            Option(partnerUserDao.findByEmail(email)).cata(
                pu => {
                    val pus = new PartnerUserServiceActorClient(context.actorOf(Props().withCreator {
                        val p = Option(partnerUserDao.findByEmail(email)).get
                        new PartnerUserServiceActor(p, partnerUserDao, partnerViewDao)
                    }))
                    cache(email) = pus
                    channel ! CreatePartnerUserServiceResponse(msg, Right(pus))
                },
                channel ! CreatePartnerUserServiceResponse(msg, Left(new PartnerUserException(
                    "No user with email %s" format email))))

        case msg @ CacheEntryRemoved(partnerUserId: String, pus: PartnerUserService, cause: String) =>
            logger.debug("Received {}", msg)
            pus.logout(partnerUserId)
            for((e, s) <- cache.view if (s.id == pus.id)) {
                cache -= e
                logger.debug("Removed {} from cache", pus.id)
            }
            logger.debug("Sent logout for {}", pus)

        case msg: Login => login(msg, context.sender)
        case msg @ Logout(partnerUserId) =>
            val channel = context.sender

            try {
                logger.debug("Processing {}", msg)
                cacheById.remove(partnerUserId).cata(
                    pus => {
                        channel ! LogoutResponse(msg, Right(true))
                        logger.debug("Successfully logged out {}", partnerUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        logger.debug("Did not find PartnerUser {} to logout", partnerUserId)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(PartnerUserException("Could not logout partner", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case msg @ Locate(partnerUserId) =>
            context.sender ! LocateResponse(msg, cacheById.get(partnerUserId).toRight(LoginError("No partner user")))

    }

    }), "PartnerUserServiceManager")

}
