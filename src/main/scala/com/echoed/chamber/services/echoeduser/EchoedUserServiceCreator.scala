package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService

trait EchoedUserServiceCreator {

    def createEchoedUserServiceUsingId(id: String): Future[EchoedUserService]

    def createEchoedUserServiceUsingFacebookService(facebookService: FacebookService): Future[EchoedUserService]

    def createEchoedUserServiceUsingTwitterService(twitterService: TwitterService): Future[EchoedUserService]
}