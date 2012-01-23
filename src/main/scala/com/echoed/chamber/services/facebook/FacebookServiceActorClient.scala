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
            (facebookServiceActor ? GetFacebookUser()).mapTo[GetFacebookUserResponse]

    def assignEchoedUser(echoedUser: EchoedUser) =
            (facebookServiceActor ? AssignEchoedUser(echoedUser)).mapTo[AssignEchoedUserResponse]

    def echo(echo: Echo, message: String) =
            (facebookServiceActor ? EchoToFacebook(echo, message)).mapTo[EchoToFacebookResponse]

    def getFacebookFriends() =
            (facebookServiceActor ? GetFriends(null, null, null)).mapTo[GetFriendsResponse]

    private[services] def fetchFacebookFriends() =
            (facebookServiceActor ? '_fetchFacebookFriends).mapTo[GetFriendsResponse]

    def logout(facebookUserId: String) =
            (facebookServiceActor ? Logout(facebookUserId)).mapTo[LogoutResponse]

    override def toString = id
}
