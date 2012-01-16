package com.echoed.chamber.services.facebook

import akka.actor.ActorRef
import com.echoed.chamber.domain._
import com.echoed.chamber.services.ActorClient


class FacebookServiceActorClient(facebookServiceActor: ActorRef)
        extends FacebookService
        with ActorClient
        with Serializable {

    val id = facebookServiceActor.id

    def actorRef = facebookServiceActor

    def getFacebookUser =
            (facebookServiceActor ? "facebookUser").mapTo[FacebookUser]

    def assignEchoedUser(echoedUser: EchoedUser) =
            (facebookServiceActor ? ("assignEchoedUser", echoedUser)).mapTo[FacebookUser]

    def echo(echo: Echo, message: String) =
            (facebookServiceActor ? ("echo", echo, message)).mapTo[FacebookPost]

    def getFacebookFriends() =
            (facebookServiceActor ? 'getFacebookFriends).mapTo[List[FacebookFriend]]

    private[services] def fetchFacebookFriends() =
            (facebookServiceActor ? '_fetchFacebookFriends).mapTo[GetFriendsResponse]

    def logout(facebookUserId: String) =
            (facebookServiceActor ? Logout(facebookUserId)).mapTo[LogoutResponse]

    override def toString = id
}
