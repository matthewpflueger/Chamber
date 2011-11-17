package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}


trait FacebookAccess {

    def getAccessToken(code: String): Future[String]
    def getMe(accessToken: String): Future[FacebookUser]
    def getFriends(accessToken: String): Future[List[FacebookFriend]]
    def post(accessToken: String, facebookId: String, facebookPost: FacebookPost): Future[FacebookPost]

}
