package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.dao.EchoedUserDao


class EchoedUserServiceActor(
        echoedUser: EchoedUser,
        echoedUserDao: EchoedUserDao) extends Actor {

    def receive = {
        case _ => throw new RuntimeException("Implement me!")
    }
}