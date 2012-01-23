package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}


trait FacebookAccess {

    def getMe(code: String, queryString: String): Future[GetMeResponse]

    def getFriends(accessToken: String, facebookId: String, facebookUserId: String): Future[GetFriendsResponse]

    def post(accessToken: String, facebookId: String, facebookPost: FacebookPost): Future[PostResponse]

    def logout(accessToken: String): Future[LogoutResponse]

}
