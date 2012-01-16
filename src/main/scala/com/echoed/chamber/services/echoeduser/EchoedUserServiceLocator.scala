package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService

trait EchoedUserServiceLocator {

    def getEchoedUserServiceWithId(id: String): Future[LocateWithIdResponse]

    def getEchoedUserServiceWithFacebookService(facebookService: FacebookService): Future[LocateWithFacebookServiceResponse]

    def getEchoedUserServiceWithTwitterService(twitterService: TwitterService): Future[LocateWithTwitterServiceResponse]

    def logout(id: String): Future[LogoutResponse]
}
