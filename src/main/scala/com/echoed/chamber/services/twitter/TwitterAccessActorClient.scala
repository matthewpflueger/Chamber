package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import com.echoed.chamber.domain.{TwitterFollower, TwitterUser,TwitterStatus}
import reflect.BeanProperty
import akka.actor.{Actor, ActorRef}
import twitter4j.auth.{RequestToken,AccessToken}
import twitter4j.Status

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

  def getFollowers(accessToken:String,  accessTokenSecret:String, userId: Long) = {
    Future[Array[TwitterFollower]]{
          (twitterAccessActor ? ("getFollowers",accessToken, accessTokenSecret,userId)).get.asInstanceOf[Array[TwitterFollower]]
    }
  }

  def updateStatus(accessToken:String, accessTokenSecret: String, status:String) ={
      Future[TwitterStatus]{
            (twitterAccessActor ? ("updateStatus",accessToken,accessTokenSecret,status)).get.asInstanceOf[TwitterStatus]
      }
  }

  def getStatus(accessToken:String, accessTokenSecret:String, statusId:String) = {
      Future[TwitterStatus]{
            (twitterAccessActor ? ("getStatus",accessToken, accessTokenSecret,statusId)).get.asInstanceOf[TwitterStatus]
      }
  }

  def getRetweetIds(accessToken:String, accessTokenSecret:String,  tweetId:String) = {
      Future[Array[Long]]{
            (twitterAccessActor ? ("getRetweetIds",accessToken, accessTokenSecret,tweetId)).get.asInstanceOf[Array[Long]]
      }
  }

}