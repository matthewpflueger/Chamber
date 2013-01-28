package com.echoed.chamber.services

import akka.event.LoggingReceive
import akka.actor.{Stash, ReceiveTimeout, PoisonPill, ActorRef}
import scala.concurrent.duration._

abstract class OnlineOfflineService extends EchoedService with Stash {

    protected def init: Receive

    protected def online: Receive

    protected def offline: Receive = unhandledMessage

    protected def lifespan = context.setReceiveTimeout(15.minutes)

    //init our lifespan
    lifespan

    protected def receiveTimeout: Receive = {
        case ReceiveTimeout =>
            log.debug("Timeout of {} reached, terminating", context.receiveTimeout)
            self ! PoisonPill
    }

    final protected def handle: Receive = init.orElse(receiveTimeout).orElse(unhandledMessage)

    protected var unhandledMessages = List[(Message, ActorRef)]()

    protected def unhandledMessage: Receive = {
        case m: Message => stash()
    }

    protected def discardOldBehavior = true

    protected def becomeOnline = {
        context.become(LoggingReceive(online.orElse(receiveTimeout)), discardOldBehavior)
        unstashAll()
    }

    protected def becomeOffline = {
        context.become(LoggingReceive(offline.orElse(receiveTimeout).orElse(unhandledMessage)), discardOldBehavior)
        log.info("Now offline")
    }
}

