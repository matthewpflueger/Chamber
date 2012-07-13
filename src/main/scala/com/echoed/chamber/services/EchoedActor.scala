package com.echoed.chamber.services

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive

abstract class EchoedActor extends Actor with ActorLogging {

    protected def handle: Receive

    final protected def receive = LoggingReceive(handle)
}
