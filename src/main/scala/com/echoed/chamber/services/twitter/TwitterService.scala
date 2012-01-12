package com.echoed.chamber.services.twitter

import twitter4j.auth.RequestToken
import twitter4j.auth.AccessToken
import akka.dispatch.Future
import com.echoed.util.FutureHelper
import com.echoed.chamber.domain.{TwitterUser, TwitterStatus, TwitterFollower,Echo}


trait TwitterService {

    def getRequestToken: Future[GetRequestTokenResponse]

    def getAccessToken(oAuthVerifier: String): Future[GetAccessTokenResponse]

    def getUser: Future[GetUserResponse]

    def getFollowers: Future[GetFollowersResponse]

    def assignEchoedUser(echoedUserId: String): Future[AssignEchoedUserResponse]

    def tweet(echo:Echo, message:String): Future[TweetResponse]

}
