package com.echoed.chamber.services.twitter

import reflect.BeanProperty
import akka.actor.ActorRef
import twitter4j.auth.AccessToken

class TwitterServiceLocatorActorClient extends TwitterServiceLocator {

    @BeanProperty var twitterServiceLocatorActor: ActorRef = _

    def getTwitterService(callbackUrl: String) =
            (twitterServiceLocatorActor ? ("none", callbackUrl)).mapTo[TwitterService]

    def getTwitterServiceWithToken(oAuthToken:String) =
            (twitterServiceLocatorActor ? ("requestToken", oAuthToken)).mapTo[TwitterService]

    def getTwitterServiceWithAccessToken(accessToken:AccessToken) =
            (twitterServiceLocatorActor ? ("accessToken", accessToken)).mapTo[TwitterService]

    def getTwitterServiceWithId(twitterUserId: String) =
            (twitterServiceLocatorActor ? ("id", twitterUserId)).mapTo[TwitterService]

}
