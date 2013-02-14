package com.echoed.chamber.services

import akka.actor.{ActorLogging, Actor}

abstract class EchoedService extends Actor with ActorLogging {

    protected def handle: Receive

    final def receive = EchoedLoggingReceive(Some(this), handle)
}
