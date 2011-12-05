package com.echoed.chamber.services.facebook

import akka.actor.Actor
import collection.mutable.WeakHashMap
import reflect.BeanProperty
import akka.util.Duration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory


class FacebookServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[FacebookServiceLocatorActor])

    @BeanProperty var facebookServiceCreator: FacebookServiceCreator = _

    private val cache = WeakHashMap[String, FacebookService]()
    private val cacheByFacebookUserId = WeakHashMap[String, FacebookService]()

    def receive = {
        case ("code", code: String, queryString: String) => {
            logger.debug("Locating FacebookService with code {}", code)
            val channel = self.channel
            cache.get(code) match {
                case Some(facebookService) =>
                    logger.debug("Cache hit for code {}", code)
                    channel ! facebookService
                case None =>
                    logger.debug("Cache miss for FacebookService key {}", code)
                    facebookServiceCreator.createFacebookServiceUsingCode(code, queryString).map { facebookService =>
                        cache += (code -> facebookService)
                        facebookService.getFacebookUser.map { facebookUser =>
                            cacheByFacebookUserId += (facebookUser.id -> facebookService)
                        }
                        logger.debug("Seeded cache with FacebookService key {}", code)
                        channel ! facebookService
                    }
            }
        }
        case ("facebookUserId", facebookUserId: String) =>
            cacheByFacebookUserId.get(facebookUserId) match {
                case Some(facebookService) =>
                    logger.debug("Cache hit for FacebookService with facebookUserId {}", facebookUserId)
                    self.channel ! facebookService
                case None =>
                    logger.debug("Cache miss for FacebookService with facebookUserId {}", facebookUserId)
                    val channel = self.channel
                    facebookServiceCreator.createFacebookServiceUsingFacebookUserId(facebookUserId).map { facebookService =>
                        cacheByFacebookUserId += (facebookUserId -> facebookService)
                        channel ! facebookService
                    }
            }
    }


}
