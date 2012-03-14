package com.echoed.cache

import com.echoed.chamber.services.ActorClient
import akka.actor.ActorRef
import org.slf4j.LoggerFactory


class CacheListenerActorClient(val actorRef: ActorRef) extends CacheListener with ActorClient {

    private val logger = LoggerFactory.getLogger(classOf[CacheListenerActorClient])

    def onRemoval(key: String, value: AnyRef, cause: String) {
        logger.debug("Cache entry {} removed due to {}", key, cause)
        //we are ignoring null values as they hold no interest to us at this time...
        Option(value).foreach(actorRef ! CacheEntryRemoved(key, _, cause))
    }
}

case class CacheEntryRemoved(key: String, value: AnyRef, cause: String)
