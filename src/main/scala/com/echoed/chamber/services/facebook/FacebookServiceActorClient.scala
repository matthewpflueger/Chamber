package com.echoed.chamber.services.facebook

import akka.actor.ActorRef
import com.echoed.chamber.domain._


class FacebookServiceActorClient(facebookServiceActor: ActorRef) extends FacebookService {

    def getFacebookUser =
            (facebookServiceActor ? "facebookUser").mapTo[FacebookUser]

    def assignEchoedUser(echoedUser: EchoedUser) =
            (facebookServiceActor ? ("assignEchoedUser", echoedUser)).mapTo[FacebookUser]

    def echo(echo: Echo, message: String) =
            (facebookServiceActor ? ("echo", echo, message)).mapTo[FacebookPost]

    def getFacebookFriends() =
            (facebookServiceActor ? 'getFacebookFriends).mapTo[List[FacebookFriend]]

    private[services] def fetchFacebookFriends() =
            (facebookServiceActor ? '_fetchFacebookFriends).mapTo[List[FacebookFriend]]
}
