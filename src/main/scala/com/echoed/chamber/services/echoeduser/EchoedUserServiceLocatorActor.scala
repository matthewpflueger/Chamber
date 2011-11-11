package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import collection.mutable.WeakHashMap
import reflect.BeanProperty
import org.slf4j.LoggerFactory


class EchoedUserServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceLocatorActor])

    @BeanProperty var echoedUserServiceCreator: EchoedUserServiceCreator = null

    private val cache = WeakHashMap[String, EchoedUserService]()

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
    }


}