package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future
import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService

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

    def getEchoedUserServiceWithTwitterService(twitterService:TwitterService) = {
        Future[EchoedUserService] {
            (echoedUserServiceLocatorActor ? ("twitterService", twitterService)).get.asInstanceOf[EchoedUserService]
        }
    }
}