package com.echoed.chamber.services.facebook

import com.echoed.chamber.dao.FacebookUserDao
import akka.actor.Actor
import com.echoed.chamber.domain.{EchoedUser, FacebookUser}


class FacebookServiceActor(
        facebookUser: FacebookUser,
        facebookAccess: FacebookAccess,
        facebookUserDao: FacebookUserDao) extends Actor {

    def receive = {
        case "facebookUser" => self.channel ! facebookUser
        case ("assignEchoedUser", echoedUser: EchoedUser) =>
                facebookUser.echoedUserId = echoedUser.id
                facebookUserDao.insertOrUpdate(facebookUser)
                self.channel ! facebookUser
    }
}
