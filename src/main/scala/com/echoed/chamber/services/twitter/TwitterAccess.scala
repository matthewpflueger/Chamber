package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import com.echoed.chamber.domain.{TwitterFollower, TwitterUser}
import twitter4j.auth.{RequestToken,AccessToken}

trait TwitterAccess {

    def getRequestToken(): Future[RequestToken]
    def getAccessToken(requestToken:RequestToken, oAuthVerifier:String): Future[AccessToken]

    def getAccessToken(accessToken:String, accessTokenSecret: String): Future[AccessToken]

    def getUser(accessToken:String, accessTokenSecret:String,userId:Long): Future[TwitterUser]

    def updateStatus(accessToken:String, accessTokenSecret:String,  status:String ): Future[String]

}