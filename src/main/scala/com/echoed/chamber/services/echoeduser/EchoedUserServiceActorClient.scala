package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import akka.dispatch.Future._
import akka.dispatch.Future
import com.echoed.chamber.domain.{EchoedUser, EchoPossibility}


class EchoedUserServiceActorClient(echoedUserServiceActor: ActorRef) extends EchoedUserService {

    def getEchoedUser() =
        Future[EchoedUser] {
            (echoedUserServiceActor ? "echoedUser").get.asInstanceOf[EchoedUser]
        }
}
