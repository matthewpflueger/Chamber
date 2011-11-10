package com.echoed.chamber.services.twitter

import akka.dispatch.{CompletableFuture, Future}
import twitter4j.auth.AccessToken

trait TwitterServiceCreator {
  def createTwitterService(): Future[TwitterService]
  def createTwitterServiceWithAccessToken(accessToken:String,accessTokenSecret:String): Future[TwitterService]
}