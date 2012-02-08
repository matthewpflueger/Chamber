package com.echoed.chamber.services.adminuser

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import akka.actor._
import scala.collection.mutable.{ConcurrentMap, WeakHashMap}



class AdminUserServiceLocatorActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[AdminUserServiceLocatorActor])

    @BeanProperty var adminUserServiceCreator: AdminUserServiceCreator = _
    @BeanProperty var cacheManager: CacheManager = _

    private val cache = WeakHashMap[String, AdminUserService]()
    private var cacheById: ConcurrentMap[String, AdminUserService] = null

    override def preStart() {
        cacheById = cacheManager.getCache[AdminUserService](
            "AdminUserServices",
            Some(new CacheListenerActorClient(self)))
    }

    def login(msg: Login, channel: Channel[LoginResponse]) {
        val email = msg.email
        val password = msg.password
        logger.debug("Locating AdminUserService for {}", email)
        cache.get(email) match {
            case Some(adminUserService) =>
                logger.debug("Cache hit for {}", email)
                adminUserService.getAdminUser.onResult {
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
                adminUserServiceCreator.createAdminUserService(email).onResult {
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
        case msg @ CacheEntryRemoved(adminUserId: String, aus: AdminUserService, cause: String) =>
            logger.debug("Received {}", msg)
            aus.logout(adminUserId)
            for((e, s) <- cache.view if (s.id == aus.id)) {
                cache -= e
                logger.debug("Removed {} from cache", aus.id)
            }
            logger.debug("Sent logout for {}", aus)

        case msg: CreateAdminUser =>
            val channel = self.channel
            adminUserServiceCreator.createAdminUser(msg.email,msg.name,msg.password).onResult{
                case CreateAdminUserResponse(_, Left(e)) =>
                    channel ! CreateAdminUserResponse(msg, Left(e))
                case CreateAdminUserResponse(_, Right(adminUser)) =>
                    channel ! CreateAdminUserResponse(msg, Right(adminUser))
            }

        case msg: Login => login(msg, self.channel)
        case msg @ Logout(adminUserId) =>
            val channel = self.channel

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
            self.channel ! LocateAdminUserServiceResponse(msg, cacheById.get(adminUserId).toRight(LoginError("No partner user")))

    }

}
