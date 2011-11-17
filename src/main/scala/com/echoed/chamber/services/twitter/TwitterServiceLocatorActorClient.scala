package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import reflect.BeanProperty
import akka.actor.{ActorRef, TypedActor}
import twitter4j.auth.AccessToken

class TwitterServiceLocatorActorClient extends TwitterServiceLocator {


  @BeanProperty var actorRef: ActorRef = null

  def getTwitterService()={
    Future[TwitterService]{
      (actorRef ? ("none")).get.asInstanceOf[TwitterService]
    }
  }

  def getTwitterServiceWithToken(oAuthToken:String) ={
    Future[TwitterService]{
      (actorRef ? ("requestToken", oAuthToken)).get.asInstanceOf[TwitterService]
    }
  }

  def getTwitterServiceWithAccessToken(accessToken:AccessToken)={
    Future[TwitterService]{
      (actorRef ? ("accessToken", accessToken)).get.asInstanceOf[TwitterService]
    }
  }

  def getTwitterServiceWithId(twitterUserId: String) = {
    Future[TwitterService]{
      (actorRef ? ("id", twitterUserId)).get.asInstanceOf[TwitterService]
    }
  }

}