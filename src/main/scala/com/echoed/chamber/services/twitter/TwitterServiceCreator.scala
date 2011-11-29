package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import twitter4j.auth.AccessToken

trait TwitterServiceCreator {
    def createTwitterService(): Future[TwitterService]

    def createTwitterServiceWithAccessToken(accessToken: AccessToken): Future[TwitterService]

    def createTwitterServiceWithId(id: String): Future[TwitterService]
}
