package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future
import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService


class EchoedUserServiceLocatorActorClient extends EchoedUserServiceLocator {

    @BeanProperty var echoedUserServiceLocatorActor: ActorRef = null

    def getEchoedUserServiceWithId(id: String) = {
        Future[EchoedUserService]{
            (echoedUserServiceLocatorActor ? ("id", id)).get.asInstanceOf[EchoedUserService]
        }
    }

    def getEchoedUserServiceWithFacebookService(facebookService: FacebookService) = {
        Future[EchoedUserService] {
            (echoedUserServiceLocatorActor ? ("facebookService", facebookService)).get.asInstanceOf[EchoedUserService]
        }
    }
}