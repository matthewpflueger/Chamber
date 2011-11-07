package com.echoed.chamber.services.facebook

import akka.actor.Actor
import akka.dispatch.Future
import collection.mutable.WeakHashMap
import reflect.BeanProperty
import akka.util.Duration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory


class FacebookServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[FacebookServiceLocatorActor])

    @BeanProperty var facebookServiceCreator: FacebookServiceCreator = null

    private val cache = WeakHashMap[String, FacebookService]()

    def receive = {
        case ("code", code: String) => {
            logger.debug("Locating FacebookService with code {}", code)
            self.channel ! cache.getOrElse(code, {
                logger.debug("Cache miss for FacebookService key {}", code)
                val f = facebookServiceCreator.createFacebookServiceUsingCode(code).await(Duration(10, TimeUnit.SECONDS)).get
                cache += (code -> f)
                logger.debug("Seeded cache with FacebookService key {}", code)
                f
            })
        }
    }


}