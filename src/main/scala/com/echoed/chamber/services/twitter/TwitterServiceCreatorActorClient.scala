package com.echoed.chamber.services.twitter

import akka.actor.{Channel, ActorRef, Actor}
import reflect.BeanProperty
import akka.dispatch.{Promise, Future}
import twitter4j.auth.AccessToken


class TwitterServiceCreatorActorClient extends TwitterServiceCreator{

    @BeanProperty var twitterServiceCreatorActor: ActorRef = null

    def createTwitterService() = {
        Future[TwitterService] {
            (twitterServiceCreatorActor ? ("code")).get.asInstanceOf[TwitterService]
        }
    }

    def createTwitterServiceWithAccessToken(accessToken:String, accessTokenSecret: String) ={
        Future[TwitterService] {
            (twitterServiceCreatorActor ? ("accessToken",accessToken,accessTokenSecret)).get.asInstanceOf[TwitterService]
        }
    }
}