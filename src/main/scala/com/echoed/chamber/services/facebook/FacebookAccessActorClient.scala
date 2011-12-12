package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}
import akka.actor.{Actor, ActorRef}


class FacebookAccessActorClient extends FacebookAccess {

    @BeanProperty var facebookAccessActor: ActorRef = _


    def getAccessToken(code: String, queryString: String) =
            (facebookAccessActor ? ("accessToken", code, queryString)).mapTo[String]

    def getMe(accessToken: String) =
            (facebookAccessActor ? ("me", accessToken)).mapTo[FacebookUser]

    def getFriends(accessToken: String, facebookId: String, facebookUserId: String) =
            (facebookAccessActor ? ("friends", accessToken, facebookId, facebookUserId)).mapTo[List[FacebookFriend]]

    def post(accessToken: String, facebookId: String, facebookPost: FacebookPost) =
            (facebookAccessActor.?("post", accessToken, facebookId, facebookPost)(timeout = Actor.Timeout(600000L))).mapTo[FacebookPost]

}

