package com.echoed.chamber.services.echoeduser

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService

class EchoedUserServiceLocatorActorClient extends EchoedUserServiceLocator {

    @BeanProperty var echoedUserServiceLocatorActor: ActorRef = _

    def getEchoedUserServiceWithId(id: String) =
            (echoedUserServiceLocatorActor ? ("id", id)).mapTo[EchoedUserService]

    def getEchoedUserServiceWithFacebookService(facebookService: FacebookService) =
            (echoedUserServiceLocatorActor ? ("facebookService", facebookService)).mapTo[EchoedUserService]

    def getEchoedUserServiceWithTwitterService(twitterService:TwitterService) =
            (echoedUserServiceLocatorActor ? ("twitterService", twitterService)).mapTo[EchoedUserService]

}
