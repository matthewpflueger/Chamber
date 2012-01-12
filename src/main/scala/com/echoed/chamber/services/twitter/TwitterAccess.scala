package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import com.echoed.chamber.domain.TwitterStatus
import twitter4j.auth.RequestToken

trait TwitterAccess {

    def getRequestToken(callbackUrl: String): Future[FetchRequestTokenResponse]

    def getAccessToken(requestToken: RequestToken, oAuthVerifier: String): Future[GetAccessTokenForRequestTokenResponse]

    def getAccessToken(accessToken: String, accessTokenSecret: String): Future[FetchAccessTokenResponse]

    def getUser(accessToken: String, accessTokenSecret: String, userId: Long): Future[FetchUserResponse]

    def getFollowers(accessToken: String, accessTokenSecret: String, twitterUserId: String, twitterId: Long): Future[FetchFollowersResponse]

    def updateStatus(accessToken: String, accessTokenSecret: String, status: TwitterStatus): Future[UpdateStatusResponse]

}
