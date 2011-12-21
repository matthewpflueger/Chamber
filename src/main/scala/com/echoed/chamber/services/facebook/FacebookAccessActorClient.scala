package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}
import akka.actor.{Actor, ActorRef}
import com.echoed.chamber.services.ActorClient


class FacebookAccessActorClient extends FacebookAccess with ActorClient {

    @BeanProperty var actorRef: ActorRef = _


    def getAccessToken(code: String, queryString: String) =
            (actorRef ? ("accessToken", code, queryString)).mapTo[String]

    def getMe(accessToken: String) =
            (actorRef ? ("me", accessToken)).mapTo[FacebookUser]

    def getFriends(accessToken: String, facebookId: String, facebookUserId: String) =
            (actorRef ? ("friends", accessToken, facebookId, facebookUserId)).mapTo[List[FacebookFriend]]

    def post(accessToken: String, facebookId: String, facebookPost: FacebookPost) =
            (actorRef.?("post", accessToken, facebookId, facebookPost)(timeout = Actor.Timeout(600000L))).mapTo[FacebookPost]

}

