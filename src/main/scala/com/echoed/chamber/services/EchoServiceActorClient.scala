package com.echoed.chamber.services

import akka.actor.ActorRef
import akka.dispatch.Future._
import facebook.FacebookService
import akka.dispatch.Future
import reflect.BeanProperty
import com.echoed.chamber.domain.{Echo, EchoPossibility}


class EchoServiceActorClient extends EchoService {

    @BeanProperty var echoServiceActor: ActorRef = null

    def recordEchoPossibility(echoPossibility: EchoPossibility) =
        Future[EchoPossibility] {
            (echoServiceActor ? ("recordEchoPossibility", echoPossibility)).get.asInstanceOf[EchoPossibility]
        }


    def getEchoPossibility(echoPossibilityId: String) =
        Future[EchoPossibility] {
            (echoServiceActor ? ("echoPossibility", echoPossibilityId)).get.asInstanceOf[EchoPossibility]
        }


    def echo(echoedUserId: String, echoPossibilityId: String) =
        Future[Echo] {
            (echoServiceActor ? ("echo", echoedUserId, echoPossibilityId)).get.asInstanceOf[Echo]
        }

}
