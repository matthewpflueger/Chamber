package com.echoed.chamber.services.twitter

import com.echoed.chamber.services.{EchoedException, MessageResponse => MR, Message}
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views._
import twitter4j.auth.{AccessToken, RequestToken}
import akka.actor.ActorRef


sealed trait TwitterMessage extends Message

sealed case class TwitterException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)


import com.echoed.chamber.services.twitter.{TwitterMessage => TM}
import com.echoed.chamber.services.twitter.{TwitterException => TE}



case class GetAccessTokenForRequestToken(requestToken: RequestToken, oAuthVerifier: String) extends TM
case class GetAccessTokenForRequestTokenResponse(message: GetAccessTokenForRequestToken, value: Either[TE, AccessToken])
        extends TM
        with MR[AccessToken, GetAccessTokenForRequestToken, TE]

case class GetAccessToken(oAuthVerifier: String) extends TM
case class GetAccessTokenResponse(message: GetAccessToken, value: Either[TE, AccessToken])
        extends TM
        with MR[AccessToken, GetAccessToken, TE]

//case class FetchAccessToken(accessToken: String, accessTokenSecret: String) extends TM
//case class FetchAccessTokenResponse(message: FetchAccessToken, value: Either[TE, AccessToken])
//        extends TM
//        with MR[AccessToken, FetchAccessToken, TE]

case class GetUser() extends TM
case class GetUserResponse(message: GetUser, value: Either[TE, TwitterUser])
        extends TM
        with MR[TwitterUser, GetUser, TE]

case class FetchUserForAuthToken(authToken: String, authVerifier: String) extends TM
case class FetchUserForAuthTokenResponse(message: FetchUserForAuthToken, value: Either[TE, TwitterUser])
        extends TM
        with MR[TwitterUser, FetchUserForAuthToken, TE]

case class FetchUser(accessToken: String, accessTokenSecret: String, userId: Long) extends TM
case class FetchUserResponse(message: FetchUser, value: Either[TE, TwitterUser])
        extends TM
        with MR[TwitterUser, FetchUser, TE]

//case class GetFollowers(twitterUserId: String) extends TM
//case class GetFollowersResponse(message: GetFollowers, value: Either[TE, List[TwitterFollower]])
//        extends TM
//        with MR[List[TwitterFollower], GetFollowers, TE]

case class FetchFollowers(accessToken: String, accessTokenSecret: String, twitterUserId: String, twitterId: Long) extends TM
case class FetchFollowersResponse(message: FetchFollowers, value: Either[TE, List[TwitterFollower]])
        extends TM
        with MR[List[TwitterFollower], FetchFollowers, TE]

case class UpdateStatus(accessToken: String, accessTokenSecret: String, status: TwitterStatus) extends TM
case class UpdateStatusResponse(message: UpdateStatus, value: Either[TE, TwitterStatus])
        extends TM
        with MR[TwitterStatus, UpdateStatus, TE]


case class Tweet(twitterUserId: String, echo: Echo, message: String) extends TM
case class TweetResponse(message: Tweet, value: Either[TE, TwitterStatus])
        extends TM
        with MR[TwitterStatus, Tweet, TE]

case class RetryTweet(twitterStatus: TwitterStatus, retries: Int = 1) extends TM
