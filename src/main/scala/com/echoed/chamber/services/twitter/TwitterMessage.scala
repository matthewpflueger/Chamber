package com.echoed.chamber.services.twitter

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views._
import twitter4j.auth.{AccessToken, RequestToken}


sealed trait TwitterMessage extends Message

sealed case class TwitterException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)


import com.echoed.chamber.services.twitter.{TwitterMessage => TM}
import com.echoed.chamber.services.twitter.{TwitterException => TE}


case class GetRequestToken() extends TM
case class GetRequestTokenResponse(message: GetRequestToken, value: Either[TE, RequestToken])
        extends TM
        with RM[RequestToken, GetRequestToken, TE]

case class FetchRequestToken(callbackUrl: String) extends TM
case class FetchRequestTokenResponse(message: FetchRequestToken, value: Either[TE, RequestToken])
        extends TM
        with RM[RequestToken, FetchRequestToken, TE]

case class GetAccessTokenForRequestToken(requestToken: RequestToken, oAuthVerifier: String) extends TM
case class GetAccessTokenForRequestTokenResponse(message: GetAccessTokenForRequestToken, value: Either[TE, AccessToken])
        extends TM
        with RM[AccessToken, GetAccessTokenForRequestToken, TE]

case class GetAccessToken(oAuthVerifier: String) extends TM
case class GetAccessTokenResponse(message: GetAccessToken, value: Either[TE, AccessToken])
        extends TM
        with RM[AccessToken, GetAccessToken, TE]

case class FetchAccessToken(accessToken: String, accessTokenSecret: String) extends TM
case class FetchAccessTokenResponse(message: FetchAccessToken, value: Either[TE, AccessToken])
        extends TM
        with RM[AccessToken, FetchAccessToken, TE]

case class GetUser() extends TM
case class GetUserResponse(message: GetUser, value: Either[TE, TwitterUser])
        extends TM
        with RM[TwitterUser, GetUser, TE]

case class FetchUser(accessToken: String, accessTokenSecret: String, userId: Long) extends TM
case class FetchUserResponse(message: FetchUser, value: Either[TE, TwitterUser])
        extends TM
        with RM[TwitterUser, FetchUser, TE]

case class GetFollowers() extends TM
case class GetFollowersResponse(message: GetFollowers, value: Either[TE, List[TwitterFollower]])
        extends TM
        with RM[List[TwitterFollower], GetFollowers, TE]

case class FetchFollowers(accessToken: String, accessTokenSecret: String, twitterUserId: String, twitterId: Long) extends TM
case class FetchFollowersResponse(message: FetchFollowers, value: Either[TE, List[TwitterFollower]])
        extends TM
        with RM[List[TwitterFollower], FetchFollowers, TE]

case class UpdateStatus(accessToken: String, accessTokenSecret: String, status: TwitterStatus) extends TM
case class UpdateStatusResponse(message: UpdateStatus, value: Either[TE, TwitterStatus])
        extends TM
        with RM[TwitterStatus, UpdateStatus, TE]

case class CreateTwitterService(callbackUrl: String) extends TM
case class CreateTwitterServiceResponse(message: CreateTwitterService, value: Either[TE, TwitterService])
        extends TM
        with RM[TwitterService, CreateTwitterService, TE]

case class CreateTwitterServiceWithAccessToken(accessToken: AccessToken) extends TM
case class CreateTwitterServiceWithAccessTokenResponse(message: CreateTwitterServiceWithAccessToken, value: Either[TE, TwitterService])
        extends TM
        with RM[TwitterService, CreateTwitterServiceWithAccessToken, TE]

case class CreateTwitterServiceWithId(twitterUserId: String) extends TM
case class CreateTwitterServiceWithIdResponse(message: CreateTwitterServiceWithId, value: Either[TE, TwitterService])
        extends TM
        with RM[TwitterService, CreateTwitterServiceWithId, TE]

case class TwitterUserNotFound(twitterUserId: String, m: String = "Twitter user not found") extends TE(m)


case class GetTwitterService(callbackUrl: String) extends TM
case class GetTwitterServiceResponse(message: GetTwitterService, value: Either[TE, TwitterService])
        extends TM
        with RM[TwitterService, GetTwitterService, TE]

case class GetTwitterServiceWithToken(oAuthToken: String) extends TM
case class GetTwitterServiceWithTokenResponse(message: GetTwitterServiceWithToken, value: Either[TE, TwitterService])
        extends TM
        with RM[TwitterService, GetTwitterServiceWithToken, TE]

case class GetTwitterServiceWithAccessToken(accessToken: AccessToken) extends TM
case class GetTwitterServiceWithAccessTokenResponse(message: GetTwitterServiceWithAccessToken, value: Either[TE, TwitterService])
        extends TM
        with RM[TwitterService, GetTwitterServiceWithAccessToken, TE]

case class GetTwitterServiceWithId(twitterUserId: String) extends TM
case class GetTwitterServiceWithIdResponse(message: GetTwitterServiceWithId, value: Either[TE, TwitterService])
        extends TM
        with RM[TwitterService, GetTwitterServiceWithId, TE]

case class GetStatusData(twitterStatusData: TwitterStatusData) extends TM
case class GetStatusDataResponse(message: GetStatusData, value: Either[TE,  TwitterStatusData])
    extends TM 
    with RM[TwitterStatusData, GetStatusData, TE]

case class AssignEchoedUser(echoedUserId: String) extends TM
case class AssignEchoedUserResponse(message: AssignEchoedUser, value: Either[TE, TwitterUser])
        extends TM
        with RM[TwitterUser, AssignEchoedUser, TE]

case class Tweet(echo: Echo, message: String) extends TM
case class TweetResponse(message: Tweet, value: Either[TE, TwitterStatus])
        extends TM
        with RM[TwitterStatus, Tweet, TE]

case class Logout(twitterUserId: String) extends TM
case class LogoutResponse(message: Logout, value: Either[TE, Boolean])
    extends TM with RM[Boolean, Logout, TE]

case class RetryTweet(twitterStatus: TwitterStatus, retries: Int = 1) extends TM
