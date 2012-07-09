package com.echoed.chamber.services.partneruser

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
import akka.util.duration._
import akka.actor.SupervisorStrategy.Restart
import akka.pattern.ask
import akka.util.Timeout


class PartnerUserServiceLocatorActor(
        cacheManager: CacheManager,
        encrypter: Encrypter,
        partnerDao: PartnerDao,
        partnerUserDao: PartnerUserDao,
        partnerViewDao: PartnerViewDao,
        implicit val timeout: Timeout = Timeout(20000)) extends Actor with ActorLogging {

    private val cache: ConcurrentMap[String, PartnerUserService] = new ConcurrentHashMap[String, PartnerUserService]()
    private var cacheById = cacheManager.getCache[PartnerUserService]("PartnerUserServices", Some(new CacheListenerActorClient(self)))

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    def login(msg: Login, channel: ActorRef) {
        val email = msg.email
        val password = msg.password
        log.debug("Locating PartnerService for {}", email)

        cache.get(email) match {
            case Some(partnerUserService) =>
                log.debug("Cache hit for {}", email)
                partnerUserService.getPartnerUser.onSuccess {
                    case GetPartnerUserResponse(_, Right(pu)) =>
                        if (pu.isPassword(password)) {
                            cacheById.put(pu.id, partnerUserService)
                            channel ! LoginResponse(msg, Right(partnerUserService))
                            log.debug("Valid login for {}", email)
                        } else {
                            channel ! LoginResponse(msg, Left(LoginError("Invalid login")))
                            log.debug("Invalid login for {}", email)
                        }
                    case GetPartnerUserResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        log.error("Error getting PartnerUser from PartneruserService for {}", email)
                }

            case None =>
                log.debug("Cache miss for {}", email)
                (self ? CreatePartnerUserService(email)).onSuccess {
                    case CreatePartnerUserServiceResponse(_, Right(pus)) =>
                        log.debug("Seeded cache for {}", email)
                        login(msg, channel)
                    case CreatePartnerUserServiceResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        log.debug("Invalid login for {}", email)
                }
        }
    }


    def receive = {

        case msg @ CreatePartnerUserService(email) =>
            val channel = context.sender

            log.debug("Loading PartnerUser for {}", email)
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
            log.debug("Received {}", msg)
            pus.logout(partnerUserId)
            for((e, s) <- cache.view if (s.id == pus.id)) {
                cache -= e
                log.debug("Removed {} from cache", pus.id)
            }
            log.debug("Sent logout for {}", pus)

        case msg: Login => login(msg, context.sender)
        case msg @ Logout(partnerUserId) =>
            val channel = context.sender

            try {
                log.debug("Processing {}", msg)
                cacheById.remove(partnerUserId).cata(
                    pus => {
                        channel ! LogoutResponse(msg, Right(true))
                        log.debug("Successfully logged out {}", partnerUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        log.debug("Did not find PartnerUser {} to logout", partnerUserId)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(PartnerUserException("Could not logout partner", e)))
                    log.error("Unexpected error processing %s" format msg, e)
            }

        case msg @ Locate(partnerUserId) =>
            context.sender ! LocateResponse(msg, cacheById.get(partnerUserId).toRight(LoginError("No partner user")))

    }

}
