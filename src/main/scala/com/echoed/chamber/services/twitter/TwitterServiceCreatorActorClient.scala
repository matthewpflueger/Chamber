package com.echoed.chamber.services.twitter

import akka.actor.ActorRef
import reflect.BeanProperty
import twitter4j.auth.AccessToken


class TwitterServiceCreatorActorClient extends TwitterServiceCreator{

    @BeanProperty var twitterServiceCreatorActor: ActorRef = _

    def createTwitterService() =
            (twitterServiceCreatorActor ? ("code")).mapTo[TwitterService]

    def createTwitterServiceWithAccessToken(accessToken:AccessToken) =
            (twitterServiceCreatorActor ? ("accessToken",accessToken)).mapTo[TwitterService]

    def createTwitterServiceWithId(id: String) =
            (twitterServiceCreatorActor ? ("id",id)).mapTo[TwitterService]

}
