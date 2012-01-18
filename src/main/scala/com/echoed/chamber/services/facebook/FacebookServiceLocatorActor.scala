package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import akka.actor.{Channel, Actor}
import com.echoed.cache.{CacheEntryRemoved, CacheManager, CacheListenerActorClient}
import scala.collection.mutable.ConcurrentMap


class FacebookServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[FacebookServiceLocatorActor])

    @BeanProperty var facebookServiceCreator: FacebookServiceCreator = _
    @BeanProperty var cacheManager: CacheManager = _


    private var cache: ConcurrentMap[String, FacebookService] = null


    override def preStart() {
        cache = cacheManager.getCache[FacebookService]("FacebookServices", Some(new CacheListenerActorClient(self)))
    }

    def receive = {
        case msg @ CacheEntryRemoved(facebookUserId: String, facebookService: FacebookService, cause: String) =>
            logger.debug("Received {}", msg)
            facebookService.logout(facebookUserId)
            logger.debug("Sent logout for {}", facebookService)

        case ("code", code: String, queryString: String) => {
            val channel = self.channel

            logger.debug("Locating FacebookService with code {}", code)
            facebookServiceCreator.createFacebookServiceUsingCode(code, queryString).map { facebookService =>
                channel ! facebookService
                facebookService.getFacebookUser.map { facebookUser =>
                    cache.put(facebookUser.id, facebookService)
                    logger.debug("Seeded cache with FacebookService key {}", code)
                }
            }
        }

        case ("facebookUserId", facebookUserId: String) =>
            cache.get(facebookUserId) match {
                case Some(facebookService) =>
                    logger.debug("Cache hit for FacebookService with facebookUserId {}", facebookUserId)
                    self.channel ! facebookService
                case None =>
                    logger.debug("Cache miss for FacebookService with facebookUserId {}", facebookUserId)
                    val channel = self.channel
                    facebookServiceCreator.createFacebookServiceUsingFacebookUserId(facebookUserId).map { facebookService =>
                        cache.put(facebookUserId, facebookService)
                        channel ! facebookService
                    }
            }

        case msg @ Logout(facebookUserId) =>
            val channel: Channel[LogoutResponse] = self.channel

            try {
                logger.debug("Processing {}", msg)
                cache.remove(facebookUserId).cata(
                    fs => {
//                        fs.logout(facebookUserId)
                        channel ! LogoutResponse(msg, Right(true))
                        logger.debug("Logged out FacebookUser {} ", facebookUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        logger.debug("Did not find FacebookUser to {}", msg)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(FacebookException("Could not logout Facebook user", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }
    }


}
