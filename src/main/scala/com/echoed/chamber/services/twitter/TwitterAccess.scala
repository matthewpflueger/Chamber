package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import com.echoed.chamber.domain.{TwitterFollower, TwitterUser, TwitterStatus}
import twitter4j.auth.{RequestToken, AccessToken}

trait TwitterAccess {

    def getRequestToken(callbackUrl: String): Future[RequestToken]

    def getAccessToken(requestToken: RequestToken, oAuthVerifier: String): Future[AccessToken]

    def getAccessToken(accessToken: String, accessTokenSecret: String): Future[AccessToken]

    def getUser(accessToken: String, accessTokenSecret: String, userId: Long): Future[TwitterUser]

    def getFollowers(accessToken: String, accessTokenSecret: String, twitterUserId: String, twitterId: Long): Future[List[TwitterFollower]]

    def updateStatus(accessToken: String, accessTokenSecret: String, status: String): Future[TwitterStatus]

    def getStatus(accessToken: String, accessTokenSecret: String, statusId: String): Future[TwitterStatus]

    def getRetweetIds(accessToken: String, accessTokenSecret: String, tweetId: String): Future[Array[Long]]


}
