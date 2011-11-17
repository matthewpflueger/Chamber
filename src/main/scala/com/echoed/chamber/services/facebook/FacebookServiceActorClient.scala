package com.echoed.chamber.services.facebook

import akka.actor.ActorRef
import akka.dispatch.Future
import com.echoed.chamber.domain.{FacebookPost, Echo, FacebookUser, EchoedUser}


class FacebookServiceActorClient(facebookServiceActor: ActorRef) extends FacebookService {

    def getFacebookUser =
        Future[FacebookUser] {
            (facebookServiceActor ? "facebookUser").get.asInstanceOf[FacebookUser]
        }

    def assignEchoedUser(echoedUser: EchoedUser) =
        Future[FacebookUser] {
            (facebookServiceActor ? ("assignEchoedUser", echoedUser)).get.asInstanceOf[FacebookUser]
        }

    def echo(echo: Echo, message: String) = (facebookServiceActor ? ("echo", echo, message)).mapTo[FacebookPost]

}
