package com.echoed.chamber.services.twitter

import com.echoed.chamber.domain.{TwitterFollower, TwitterUser,TwitterStatus}
import reflect.BeanProperty
import akka.actor.ActorRef
import twitter4j.auth.{RequestToken,AccessToken}

class TwitterAccessActorClient extends TwitterAccess {

  @BeanProperty var twitterAccessActor: ActorRef = _

  def getRequestToken(callbackUrl: String) =
          (twitterAccessActor ? ("requestToken", callbackUrl)).mapTo[RequestToken]

  def getAccessToken(requestToken: RequestToken, oAuthVerifier:String) =
          (twitterAccessActor ? ("accessToken",requestToken,oAuthVerifier)).mapTo[AccessToken]

  def getAccessToken(accessToken: String,  accessTokenSecret:String) =
          (twitterAccessActor ? ("accessToken",accessToken,accessTokenSecret)).mapTo[AccessToken]

  def getUser(accessToken:String,  accessTokenSecret:String, userId: Long) =
          (twitterAccessActor ? ("getUser", accessToken, accessTokenSecret, userId)).mapTo[TwitterUser]

  def getFollowers(accessToken:String,  accessTokenSecret:String, twitterUserId: String, twitterId: Long) =
          (twitterAccessActor ? ("getFollowers", accessToken, accessTokenSecret, twitterUserId, twitterId)).mapTo[List[TwitterFollower]]

  def updateStatus(accessToken:String, accessTokenSecret: String, status:String) =
          (twitterAccessActor ? ("updateStatus",accessToken,accessTokenSecret,status)).mapTo[TwitterStatus]

  def getStatus(accessToken:String, accessTokenSecret:String, statusId:String) =
          (twitterAccessActor ? ("getStatus",accessToken, accessTokenSecret,statusId)).mapTo[TwitterStatus]

  def getRetweetIds(accessToken:String, accessTokenSecret:String,  tweetId:String) =
          (twitterAccessActor ? ("getRetweetIds",accessToken, accessTokenSecret,tweetId)).mapTo[Array[Long]]
}
