package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import collection.mutable.WeakHashMap
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService


class EchoedUserServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceLocatorActor])

    @BeanProperty var echoedUserServiceCreator: EchoedUserServiceCreator = null

    private val cache = WeakHashMap[String, EchoedUserService]()
    private val cacheFacebookService = WeakHashMap[FacebookService, EchoedUserService]()
    private val cacheTwitterService = WeakHashMap[TwitterService,EchoedUserService]()

    def receive = {
        case ("id", id: String) => {
            logger.debug("Locating EchoedUserService with id {}", id)
            val channel = self.channel
            cache.get(id) match {
                case Some(echoedUserService) =>
                    channel ! echoedUserService
                    logger.debug("Cache hit for EchoedUserService key {}", id)
                case _ =>
                    logger.debug("Cache miss for EchoedUserService key {}", id)
                    echoedUserServiceCreator.createEchoedUserServiceUsingId(id).map { echoedUserService =>
                        channel ! echoedUserService
                        cache += (id -> echoedUserService)
                        logger.debug("Seeded cache with EchoedUserService key {}", id)
                    }
            }
        }

        case ("facebookService", facebookService: FacebookService) => {
            logger.debug("Locating EchoedUserService with {}", facebookService)
            val channel = self.channel
            cacheFacebookService.get(facebookService) match {
                case Some(echoedUserService) =>
                    channel ! echoedUserService
                    logger.debug("Cache hit for EchoedUserService with {}", facebookService)
                case _ =>
                    logger.debug("Cache miss for EchoedUserService with {}", facebookService)
                    echoedUserServiceCreator.createEchoedUserServiceUsingFacebookService(facebookService).map { echoedUserService =>
                        channel ! echoedUserService
                        cacheFacebookService += (facebookService -> echoedUserService)
                        logger.debug("Seeded cacheFacebookService with EchoedUserService key {}", facebookService)
                    }
            }
        }

        case ("twitterService", twitterService: TwitterService) => {
            logger.debug("Locating EchoedUserService with TwitterService: {}", twitterService)
            val channel = self.channel
            cacheTwitterService.get(twitterService) match {
                case Some(echoedUserService) =>
                    channel ! echoedUserService
                    logger.debug("Cache hit for EchoedUserService with {}", twitterService)
                case _ =>
                    echoedUserServiceCreator.createEchoedUserServiceUsingTwitterService(twitterService).map { echoedUserService =>
                        channel ! echoedUserService
                        cacheTwitterService += (twitterService -> echoedUserService)
                        logger.debug("Seeded cacheTwitterService with EchoedUserService key {}", twitterService)
                    }
            }
        }
    }


}
