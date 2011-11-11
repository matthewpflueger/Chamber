package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import reflect.BeanProperty
import akka.dispatch.Future


class EchoedUserServiceCreatorActorClient extends EchoedUserServiceCreator {

    @BeanProperty var echoedUserServiceCreatorActor: ActorRef = null

    def createEchoedUserServiceUsingId(id: String) = {
        Future[EchoedUserService] {
            (echoedUserServiceCreatorActor ? ("id", id)).get.asInstanceOf[EchoedUserService]
        }
    }

}