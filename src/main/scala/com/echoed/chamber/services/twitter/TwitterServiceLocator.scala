package com.echoed.chamber.services.twitter


import akka.dispatch.Future
import twitter4j.auth.AccessToken
/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 11/7/11
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */

trait TwitterServiceLocator {

  def getTwitterService(): Future[TwitterService]
  def getTwitterServiceWithToken(oAuthToken:String): Future[TwitterService]
  def getTwitterServiceWithAccessToken(accessToken:AccessToken): Future[TwitterService]
  def getTwitterServiceWithId(twitterUserId: String): Future[TwitterService]
}