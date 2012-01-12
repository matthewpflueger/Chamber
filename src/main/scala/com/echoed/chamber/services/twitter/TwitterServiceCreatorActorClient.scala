package com.echoed.chamber.services.twitter

import akka.actor.ActorRef
import reflect.BeanProperty
import twitter4j.auth.AccessToken
import com.echoed.chamber.services.ActorClient


class TwitterServiceCreatorActorClient extends TwitterServiceCreator with ActorClient {

    @BeanProperty var twitterServiceCreatorActor: ActorRef = _

    def createTwitterService(callbackUrl: String) =
            (twitterServiceCreatorActor ? CreateTwitterService(callbackUrl)).mapTo[CreateTwitterServiceResponse]

    def createTwitterServiceWithAccessToken(accessToken: AccessToken) =
            (twitterServiceCreatorActor ? CreateTwitterServiceWithAccessToken(accessToken)).mapTo[CreateTwitterServiceWithAccessTokenResponse]

    def createTwitterServiceWithId(id: String) =
            (twitterServiceCreatorActor ? CreateTwitterServiceWithId(id)).mapTo[CreateTwitterServiceWithIdResponse]

    def actorRef = twitterServiceCreatorActor
}
