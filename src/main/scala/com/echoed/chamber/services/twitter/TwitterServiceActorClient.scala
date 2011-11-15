package com.echoed.chamber.services.twitter

import akka.actor.ActorRef
import twitter4j.auth.{RequestToken,AccessToken}
import akka.dispatch.Future
import reflect.BeanProperty
import com.echoed.chamber.domain.TwitterUser


class TwitterServiceActorClient(twitterServiceActor: ActorRef) extends TwitterService  {

  def getRequestToken() = {
    Future[RequestToken]{
      (twitterServiceActor ? ("getRequestToken")).get.asInstanceOf[RequestToken]
    }
  }

  def getAccessToken(oAuthVerifier:String) = {
    Future[AccessToken]{
      (twitterServiceActor ? ("getAccessToken",oAuthVerifier)).get.asInstanceOf[AccessToken]
    }
  }

  def getUser() = {
    Future[TwitterUser]{
       (twitterServiceActor? ("getUser")).get.asInstanceOf[TwitterUser]
    }
  }

  def getTwitterUser ={
    Future[TwitterUser]{
      (twitterServiceActor ? ("getTwitterUser")).get.asInstanceOf[TwitterUser]
    }
  }

  def assignEchoedUserId(id: String) =  {
    Future[TwitterUser] {
      (twitterServiceActor ? ("assignEchoedUserId",id)).get.asInstanceOf[TwitterUser]
    }
  }

  def updateStatus(status:String) = {
    Future[String]{
      (twitterServiceActor ? ("updateStatus",status)).get.asInstanceOf[String]
    }
  }
}