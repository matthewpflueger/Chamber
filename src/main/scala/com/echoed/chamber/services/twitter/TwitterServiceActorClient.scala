package com.echoed.chamber.services.twitter

import akka.actor.ActorRef
import twitter4j.auth.{RequestToken,AccessToken}
import akka.dispatch.Future
import com.echoed.chamber.domain.{TwitterUser,TwitterStatus,TwitterFollower}


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

  def getFollowers ={
     Future[Array[TwitterFollower]]{
       (twitterServiceActor ? ("getFollowers")).get.asInstanceOf[Array[TwitterFollower]]
     }
  }

  def assignEchoedUserId(id: String) =  {
    Future[TwitterUser] {
      (twitterServiceActor ? ("assignEchoedUserId",id)).get.asInstanceOf[TwitterUser]
    }
  }

  def updateStatus(status:String) = {
    Future[TwitterStatus]{
      (twitterServiceActor ? ("updateStatus",status)).get.asInstanceOf[TwitterStatus]
    }
  }

  def getStatus(statusId:String) = {
    Future[TwitterStatus]{
      (twitterServiceActor ? ("getStatus",statusId)).get.asInstanceOf[TwitterStatus]
    }
  }

  def getRetweetIds(tweetId:String) = {
    Future[Array[Long]]{
      (twitterServiceActor ? ("getRetweetIds",tweetId)).get.asInstanceOf[Array[Long]]
    }
  }
}