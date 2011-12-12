package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import com.echoed.util.FutureHelper
import com.echoed.chamber.domain._


trait FacebookService {

    def facebookUser: Option[FacebookUser] = FutureHelper.get[FacebookUser](getFacebookUser _)

    def getFacebookUser(): Future[FacebookUser]

    def assignEchoedUser(echoedUser: EchoedUser): Future[FacebookUser]

    def echo(echo: Echo, message: String): Future[FacebookPost]

    def getFacebookFriends(): Future[List[FacebookFriend]]

    private[services] def fetchFacebookFriends(): Future[List[FacebookFriend]]
}
