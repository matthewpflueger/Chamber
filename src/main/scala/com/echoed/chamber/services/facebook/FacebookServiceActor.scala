package com.echoed.chamber.services.facebook

import com.echoed.chamber.domain.FacebookUser
import com.echoed.chamber.dao.FacebookUserDao
import akka.actor.Actor


class FacebookServiceActor(
        facebookUser: FacebookUser,
        facebookAccess: FacebookAccess,
        facebookUserDao: FacebookUserDao) extends Actor {

    def receive = {
        case _ => throw new RuntimeException("Implement me!")
    }
}