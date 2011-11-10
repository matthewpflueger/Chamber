package com.echoed.chamber.services.twitter

import twitter4j.auth.RequestToken
import twitter4j.auth.AccessToken
import akka.dispatch.Future
import com.echoed.chamber.domain.TwitterUser

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 11/7/11
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */

trait TwitterService {
  def getRequestToken(): Future[RequestToken]
  def getAccessToken(oAuthVerifier:String): Future[AccessToken]

  def getMe(accessToken:String, accessTokenSecret:String): Future[TwitterUser]

  def updateStatus(accessToken:String,accessTokenSecret:String, status: String): Future[String]
}