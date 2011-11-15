package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import com.echoed.chamber.domain.{TwitterFollower, TwitterUser}
import reflect.BeanProperty
import akka.actor.{Actor, ActorRef}
import twitter4j.auth.{RequestToken,AccessToken}

class TwitterAccessActorClient extends TwitterAccess{

  @BeanProperty var twitterAccessActor: ActorRef = null

  def getRequestToken() = {
      Future[RequestToken] {
            (twitterAccessActor ? ("requestToken")).get.asInstanceOf[RequestToken]
      }
  }

  def getAccessToken(requestToken: RequestToken, oAuthVerifier:String) = {
      Future[AccessToken] {
            (twitterAccessActor ? ("accessToken",requestToken,oAuthVerifier)).get.asInstanceOf[AccessToken]
      }
  }

  def getAccessToken(accessToken: String,  accessTokenSecret:String) = {
      Future[AccessToken] {
            (twitterAccessActor ? ("accessToken",accessToken,accessTokenSecret)).get.asInstanceOf[AccessToken]
      }
  }

  def getUser(accessToken:String,  accessTokenSecret:String, userId: Long) = {
     Future[TwitterUser] {
            (twitterAccessActor ? ("getUser", accessToken, accessTokenSecret, userId)).get.asInstanceOf[TwitterUser]
     }
  }

  def updateStatus(accessToken:String, accessTokenSecret: String, status:String) ={
      Future[String]{
            (twitterAccessActor ? ("updateStatus",accessToken,accessTokenSecret,status)).get.asInstanceOf[String]
      }
  }

}