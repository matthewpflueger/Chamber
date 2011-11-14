package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future
import com.echoed.chamber.services.facebook.FacebookService


trait EchoedUserServiceLocator {
    def getEchoedUserServiceWithId(id: String): Future[EchoedUserService]

    def getEchoedUserServiceWithFacebookService(facebookService: FacebookService): Future[EchoedUserService]
}