package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future
import reflect.BeanProperty
import akka.actor.ActorRef


class EchoedUserServiceLocatorActorClient extends EchoedUserServiceLocator {

    @BeanProperty var echoedUserServiceLocatorActor: ActorRef = null

    def getEchoedUserServiceWithId(id: String) = {
        Future[EchoedUserService]{
            (echoedUserServiceLocatorActor ? ("id", id)).get.asInstanceOf[EchoedUserService]
        }
    }

}