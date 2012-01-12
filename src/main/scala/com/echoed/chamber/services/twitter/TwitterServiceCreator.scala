package com.echoed.chamber.services.twitter

import akka.dispatch.Future
import twitter4j.auth.AccessToken

trait TwitterServiceCreator {

    def createTwitterService(callbackUrl: String): Future[CreateTwitterServiceResponse]

    def createTwitterServiceWithAccessToken(accessToken: AccessToken): Future[CreateTwitterServiceWithAccessTokenResponse]

    def createTwitterServiceWithId(id: String): Future[CreateTwitterServiceWithIdResponse]

}
