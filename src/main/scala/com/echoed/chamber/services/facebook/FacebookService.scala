package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import com.echoed.util.FutureHelper
import com.echoed.chamber.domain._


trait FacebookService {

    def getFacebookUser(): Future[GetFacebookUserResponse]

    def assignEchoedUser(echoedUser: EchoedUser): Future[AssignEchoedUserResponse]

    def echo(echo: Echo, message: String): Future[EchoToFacebookResponse]

    def getFacebookFriends(): Future[GetFriendsResponse]

    private[services] def fetchFacebookFriends(): Future[GetFriendsResponse]

    def logout(facebookUserId: String): Future[LogoutResponse]

}
