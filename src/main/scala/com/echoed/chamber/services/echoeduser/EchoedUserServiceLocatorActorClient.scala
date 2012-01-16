package com.echoed.chamber.services.echoeduser

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.services.ActorClient

class EchoedUserServiceLocatorActorClient extends EchoedUserServiceLocator with ActorClient {

    @BeanProperty var echoedUserServiceLocatorActor: ActorRef = _

    def getEchoedUserServiceWithId(id: String) =
            (echoedUserServiceLocatorActor ? LocateWithId(id)).mapTo[LocateWithIdResponse]

    def getEchoedUserServiceWithFacebookService(facebookService: FacebookService) =
            (echoedUserServiceLocatorActor ? LocateWithFacebookService(facebookService)).mapTo[LocateWithFacebookServiceResponse]

    def getEchoedUserServiceWithTwitterService(twitterService:TwitterService) =
            (echoedUserServiceLocatorActor ? LocateWithTwitterService(twitterService)).mapTo[LocateWithTwitterServiceResponse]

    def logout(id: String) =
            (echoedUserServiceLocatorActor ? Logout(id)).mapTo[LogoutResponse]

    def actorRef = echoedUserServiceLocatorActor
}
