package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import com.echoed.util.FutureHelper
import com.echoed.chamber.domain._


trait FacebookService {

    val id: String

    def getFacebookUser(): Future[FacebookUser]

    def assignEchoedUser(echoedUser: EchoedUser): Future[FacebookUser]

    def echo(echo: Echo, message: String): Future[FacebookPost]

    def getFacebookFriends(): Future[List[FacebookFriend]]

    private[services] def fetchFacebookFriends(): Future[GetFriendsResponse]

    def logout(facebookUserId: String): Future[LogoutResponse]

}
