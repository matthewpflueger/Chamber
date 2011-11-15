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

    def createTwitterServiceWithAccessToken(accessToken:AccessToken) ={
        Future[TwitterService] {
            (twitterServiceCreatorActor ? ("accessToken",accessToken)).get.asInstanceOf[TwitterService]
        }
    }

    def createTwitterServiceWithId(id: String) = {
        Future[TwitterService]{
            (twitterServiceCreatorActor ? ("id",id)).get.asInstanceOf[TwitterService]
        }
    }
}