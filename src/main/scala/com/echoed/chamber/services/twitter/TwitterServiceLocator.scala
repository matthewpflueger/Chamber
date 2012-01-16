package com.echoed.chamber.services.twitter


import akka.dispatch.Future
import twitter4j.auth.AccessToken


trait TwitterServiceLocator {

    def getTwitterService(callbackUrl: String): Future[GetTwitterServiceResponse]

    def getTwitterServiceWithToken(oAuthToken: String): Future[GetTwitterServiceWithTokenResponse]

    def getTwitterServiceWithAccessToken(accessToken: AccessToken): Future[GetTwitterServiceWithAccessTokenResponse]

    def getTwitterServiceWithId(id: String): Future[GetTwitterServiceWithIdResponse]

    def logout(twitterUserId: String): Future[LogoutResponse]
}
