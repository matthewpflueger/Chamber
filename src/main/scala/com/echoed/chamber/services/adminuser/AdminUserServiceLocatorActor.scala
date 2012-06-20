package com.echoed.chamber.services.adminuser

import reflect.BeanProperty
import scalaz._
import Scalaz._
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import akka.actor._
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import com.echoed.chamber.dao.AdminUserDao
import com.echoed.chamber.dao.views.AdminViewDao
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import akka.util.Timeout
import akka.pattern.ask
import akka.util.duration._
import akka.event.Logging
import com.echoed.chamber.domain.AdminUser
import org.springframework.beans.factory.FactoryBean


class AdminUserServiceLocatorActor extends FactoryBean[ActorRef] {


    @BeanProperty var cacheManager: CacheManager = _
    @BeanProperty var adminUserDao: AdminUserDao = _
    @BeanProperty var adminViewDao: AdminViewDao = _
    @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _
    @BeanProperty var partnerDao: PartnerDao = _

    private val cache: ConcurrentMap[String, AdminUserService] = new ConcurrentHashMap[String, AdminUserService]()
    private var cacheById: ConcurrentMap[String, AdminUserService] = null

    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    override def preStart() {
        cacheById = cacheManager.getCache[AdminUserService](
            "AdminUserServices",
            Some(new CacheListenerActorClient(self)))
    }

    def login(msg: Login, channel: ActorRef) {
        val email = msg.email
        val password = msg.password
        logger.debug("Locating AdminUserService for {}", email)
        cache.get(email) match {
            case Some(adminUserService) =>
                logger.debug("Cache hit for {}", email)
                adminUserService.getAdminUser.onSuccess {
                    case GetAdminUserResponse(_, Right(au)) =>
                        if (au.isPassword(password)) {
                            cacheById.put(au.id, adminUserService)
                            channel ! LoginResponse(msg, Right(adminUserService))
                            logger.debug("Valid login for {}", email)
                        } else {
                            channel ! LoginResponse(msg, Left(LoginError("Invalid login")))
                            logger.debug("Invalid login for {}", email)
                        }
                    case GetAdminUserResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        logger.error("Error getting PartnerUser from PartneruserService for {}", email)
                }

            case None =>
                logger.debug("Cache miss for {}", email)
                (self ? CreateAdminUserService(email)).onSuccess {
                    case CreateAdminUserServiceResponse(_, Right(aus)) =>
                        cache(email) = aus
                        logger.debug("Seeded cache for {}", email)
                        login(msg, channel)
                    case CreateAdminUserServiceResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        logger.debug("Invalid login for {}", email)
                }
        }
    }

    def receive = {
        case msg @ CreateAdminUserService(email) =>
            val channel = context.sender

            logger.debug("Loading AdminUser for {}", email)
            Option(adminUserDao.findByEmail(email)).cata(
                au => {
                    val aus = new AdminUserServiceActorClient(context.actorOf(Props().withCreator {
                        val a = Option(adminUserDao.findByEmail(email)).get
                        new AdminUserServiceActor(a, adminUserDao, adminViewDao, partnerSettingsDao, partnerDao)
                    }))
                    cache(email) = aus
                    channel ! CreateAdminUserServiceResponse(msg, Right(aus))
                },
                channel ! CreateAdminUserServiceResponse(msg, Left(new AdminUserException(
                    "No user with email %s" format email))))

        case msg @ CreateAdminUser(email, name, password) =>
            val channel = context.sender

            logger.debug("Creating Admin User: {}:{}", name, email)
            var adminUser = new AdminUser(name, email)
            adminUser = adminUser.createPassword(password)
            logger.debug("AdminUser: {} ", adminUser)
            adminUserDao.insert(adminUser)
            channel ! CreateAdminUserResponse(msg, Right(adminUserDao.findByEmail(email)))

        case msg @ CacheEntryRemoved(adminUserId: String, aus: AdminUserService, cause: String) =>
            logger.debug("Received {}", msg)
            aus.logout(adminUserId)
            for((e, s) <- cache.view if (s.id == aus.id)) {
                cache -= e
                logger.debug("Removed {} from cache", aus.id)
            }
            logger.debug("Sent logout for {}", aus)

        case msg: Login => login(msg, context.sender)

        case msg @ Logout(adminUserId) =>
            val channel = context.sender

            try {
                logger.debug("Processing {}", msg)
                cacheById.remove(adminUserId).cata(
                pus => {
                    channel ! LogoutResponse(msg, Right(true))
                    logger.debug("Successfully logged out {}", adminUserId)
                },
                {
                    channel ! LogoutResponse(msg, Right(false))
                    logger.debug("Did not find PartnerUser {} to logout", adminUserId)
                })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(AdminUserException("Could not logout partner", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case msg @ LocateAdminUserService(adminUserId) =>
            context.sender ! LocateAdminUserServiceResponse(msg, cacheById.get(adminUserId).toRight(LoginError("No partner user")))

    }

    }), "AdminUserServiceManager")
}
