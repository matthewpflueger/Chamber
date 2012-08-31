package com.echoed.chamber.services

import akka.event.LoggingReceive
import akka.actor.{ReceiveTimeout, PoisonPill, ActorRef}
import akka.util.duration._

abstract class OnlineOfflineService extends EchoedService {

    protected def init: Receive

    protected def online: Receive

    protected def offline: Receive = unhandledMessage

    protected def lifespan = context.setReceiveTimeout(30 minutes)

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
        case m: Message =>
            log.debug("Unhandled message received while not online {}", m)
            unhandledMessages = (m, sender) :: unhandledMessages
    }

    protected def discardOldBehavior = true

    protected def becomeOnline = {
        context.become(LoggingReceive(online.orElse(receiveTimeout)), discardOldBehavior)
        log.info("Now online, replaying {} unhandled messages", unhandledMessages.length)
        unhandledMessages.reverse.foreach { tuple =>
            log.debug("Replaying {}", tuple._1)
            self.tell(tuple._1, tuple._2)
        }
        unhandledMessages = List[(Message, ActorRef)]()
    }

    protected def becomeOffline = {
        context.become(LoggingReceive(offline.orElse(receiveTimeout).orElse(unhandledMessage)), discardOldBehavior)
        log.info("Now offline")
    }
}

