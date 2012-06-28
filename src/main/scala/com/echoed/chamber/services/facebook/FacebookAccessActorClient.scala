package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}
import akka.actor.{Actor, ActorRef}
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class FacebookAccessActorClient
        extends FacebookAccess
        with ActorClient
        with Serializable {

    @BeanProperty var actorRef: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def getMe(code: String, queryString: String) =
            (actorRef ? GetMe(code, queryString)).mapTo[GetMeResponse]

    def getFriends(accessToken: String, facebookId: String, facebookUserId: String, context: ActorRef) =
            (actorRef ? GetFriends(accessToken, facebookId, facebookUserId, context)).mapTo[GetFriendsResponse]

    def post(accessToken: String, facebookId: String, facebookPost: FacebookPost) =
            (actorRef ? Post(accessToken, facebookId, facebookPost)).mapTo[PostResponse]
    
    def publishAction(accessToken: String,  action: String,  obj: String, objUrl: String) =
            (actorRef ? PublishAction(accessToken, action, obj, objUrl)).mapTo[PublishActionResponse]

    def logout(accessToken: String) =
            (actorRef ? Logout(accessToken)).mapTo[LogoutResponse]

    def fetchMe(accessToken: String) =
            (actorRef ? FetchMe(accessToken)).mapTo[FetchMeResponse]
}

