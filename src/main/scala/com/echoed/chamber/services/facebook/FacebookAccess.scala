package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}
import akka.actor.ActorRef


trait FacebookAccess {

    def fetchMe(accessToken: String): Future[FetchMeResponse]

    def getMe(code: String, queryString: String): Future[GetMeResponse]

    def getFriends(accessToken: String, facebookId: String, facebookUserId: String, context: ActorRef): Future[GetFriendsResponse]
    
    def publishAction(accessToken: String, action: String, obj: String,  objUrl: String): Future[PublishActionResponse]

    def post(accessToken: String, facebookId: String, facebookPost: FacebookPost): Future[PostResponse]

    def logout(accessToken: String): Future[LogoutResponse]

}
