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
            self.channel ! cache.getOrElse(id, {
                logger.debug("Cache miss for EchoedUserService key {}", id)
                val f = echoedUserServiceCreator.createEchoedUserServiceUsingId(id).get
                cache += (id -> f)
                logger.debug("Seeded cache with EchoedUserService key {}", id)
                f
            })
        }
        case ("facebookService", facebookService: FacebookService) => {
            logger.debug("Locating EchoedUserService with {}", facebookService)
            self.channel ! cacheFacebookService.getOrElse(facebookService, {
                logger.debug("Cache miss for EchoedUserService with {}", facebookService)
                val f = echoedUserServiceCreator.createEchoedUserServiceUsingFacebookService(facebookService).get
                cacheFacebookService += (facebookService -> f)
                logger.debug("Seeded cacheFacebookService with EchoedUserService key {}", facebookService)
                f
            })
        }

        case ("twitterService", twitterService: TwitterService) => {
            logger.debug("Locating EchoedUserService with TwitterService: {}", twitterService)
            self.channel ! cacheTwitterService.getOrElse(twitterService, {
                logger.debug("Cache miss for EchoedUserService with TwitterService {} ", twitterService)
                val t = echoedUserServiceCreator.createEchoedUserServiceUsingTwitterService(twitterService).get
                cacheTwitterService += (twitterService -> t)
                logger.debug("Seeded cacheTwitterService with EchoedUserService key {}", twitterService)
                t
            })
        }
    }


}