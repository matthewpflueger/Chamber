package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}
import akka.actor.{Actor, ActorRef}
import com.echoed.chamber.services.ActorClient


class FacebookAccessActorClient
        extends FacebookAccess
        with ActorClient
        with Serializable {

    @BeanProperty var actorRef: ActorRef = _

    def getMe(code: String, queryString: String) =
            (actorRef ? GetMe(code, queryString)).mapTo[GetMeResponse]

    def getFriends(accessToken: String, facebookId: String, facebookUserId: String) =
            (actorRef ? GetFriends(accessToken, facebookId, facebookUserId)).mapTo[GetFriendsResponse]

    def post(accessToken: String, facebookId: String, facebookPost: FacebookPost) =
            (actorRef ? Post(accessToken, facebookId, facebookPost)).mapTo[PostResponse]

    def logout(accessToken: String) =
            (actorRef ? Logout(accessToken)).mapTo[LogoutResponse]

}

