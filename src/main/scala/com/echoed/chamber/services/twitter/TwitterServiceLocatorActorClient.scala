package com.echoed.chamber.services.twitter

import reflect.BeanProperty
import akka.actor.ActorRef
import twitter4j.auth.AccessToken
import com.echoed.chamber.services.ActorClient

class TwitterServiceLocatorActorClient extends TwitterServiceLocator with ActorClient {

    @BeanProperty var twitterServiceLocatorActor: ActorRef = _

    def getTwitterService(callbackUrl: String) =
            (twitterServiceLocatorActor ? GetTwitterService(callbackUrl)).mapTo[GetTwitterServiceResponse]

    def getTwitterServiceWithToken(oAuthToken: String) =
            (twitterServiceLocatorActor ? GetTwitterServiceWithToken(oAuthToken)).mapTo[GetTwitterServiceWithTokenResponse]

    def getTwitterServiceWithAccessToken(accessToken: AccessToken) =
            (twitterServiceLocatorActor ? GetTwitterServiceWithAccessToken(accessToken)).mapTo[GetTwitterServiceWithAccessTokenResponse]

    def getTwitterServiceWithId(id: String) =
            (twitterServiceLocatorActor ? GetTwitterServiceWithId(id)).mapTo[GetTwitterServiceWithIdResponse]

    def actorRef = twitterServiceLocatorActor

    def logout(twitterUserId: String) =
            (twitterServiceLocatorActor ? Logout(twitterUserId)).mapTo[LogoutResponse]

}
