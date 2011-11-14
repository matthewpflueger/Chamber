package com.echoed.chamber.services

import akka.actor.ActorRef
import com.echoed.chamber.domain.EchoPossibility
import akka.dispatch.Future._
import facebook.FacebookService
import akka.dispatch.Future
import reflect.BeanProperty


class EchoServiceActorClient extends EchoService {

    @BeanProperty var echoServiceActor: ActorRef = null

    def recordEchoPossibility(echoPossibility: EchoPossibility) = {
        Future[EchoPossibility] {
            (echoServiceActor ? ("recordEchoPossibility", echoPossibility)).get.asInstanceOf[EchoPossibility]
        }
    }

    def getEchoPossibility(echoPossibilityId: String) =
        Future[EchoPossibility] {
            (echoServiceActor ? ("echoPossibility", echoPossibilityId)).get.asInstanceOf[EchoPossibility]
        }
}