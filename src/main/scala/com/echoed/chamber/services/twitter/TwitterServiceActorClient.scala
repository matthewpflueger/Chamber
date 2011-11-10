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

  def getMe(accessToken:String, accessTokenSecret:String) = {
    Future[TwitterUser]{
       (twitterServiceActor? ("getMe",accessToken, accessTokenSecret)).get.asInstanceOf[TwitterUser]
    }
  }

  def updateStatus(accessToken:String,accessTokenSecret: String, status:String) = {
    Future[String]{
      (twitterServiceActor ? ("updateStatus",accessToken,accessTokenSecret,status)).get.asInstanceOf[String]
    }
  }

}