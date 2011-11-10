package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import reflect.BeanProperty
import akka.actor.{ActorRef, TypedActor}

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

  def getTwitterServiceWithAccessToken(accessToken:String,accessTokenSecret: String)={
    Future[TwitterService]{
      (actorRef ? ("accessToken", accessToken, accessTokenSecret)).get.asInstanceOf[TwitterService]
    }
  }

}