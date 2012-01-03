package com.echoed.chamber.services.echoeduser

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService

class EchoedUserServiceLocatorActorClient extends EchoedUserServiceLocator {

    @BeanProperty var echoedUserServiceLocatorActor: ActorRef = _

    def getEchoedUserServiceWithId(id: String) =
            (echoedUserServiceLocatorActor ? (LocateWithId(id))).mapTo[LocateWithIdResponse]

    def getEchoedUserServiceWithFacebookService(facebookService: FacebookService) =
            (echoedUserServiceLocatorActor ? (LocateWithFacebookService(facebookService))).mapTo[LocateWithFacebookServiceResponse]

    def getEchoedUserServiceWithTwitterService(twitterService:TwitterService) =
            (echoedUserServiceLocatorActor ? (LocateWithTwitterService(twitterService))).mapTo[LocateWithTwitterServiceResponse]

}
