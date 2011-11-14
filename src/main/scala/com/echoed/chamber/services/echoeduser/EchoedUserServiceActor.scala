package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.services.facebook.FacebookService


class EchoedUserServiceActor(
        echoedUser: EchoedUser,
        echoedUserDao: EchoedUserDao,
        facebookService: FacebookService) extends Actor {

    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao) = this(echoedUser, echoedUserDao, null)

    def receive = {
        case "echoedUser" => self.channel ! echoedUser
    }
}
