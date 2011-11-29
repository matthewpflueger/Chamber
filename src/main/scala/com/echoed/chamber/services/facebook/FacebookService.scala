package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import com.echoed.util.FutureHelper
import com.echoed.chamber.domain.{FacebookPost, Echo, EchoedUser, FacebookUser}


trait FacebookService {

    def facebookUser: Option[FacebookUser] = FutureHelper.get[FacebookUser](getFacebookUser _)

    def getFacebookUser(): Future[FacebookUser]

    def assignEchoedUser(echoedUser: EchoedUser): Future[FacebookUser]

    def echo(echo: Echo, message: String): Future[FacebookPost]
}
