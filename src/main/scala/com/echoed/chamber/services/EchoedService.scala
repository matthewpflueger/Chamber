package com.echoed.chamber.services

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive

abstract class EchoedService extends Actor with ActorLogging {

    protected def handle: Receive

    final def receive = LoggingReceive(handle)
}
