package com.echoed.chamber.services.facebook

import akka.actor.Actor
import com.echoed.chamber.domain.FacebookUser
import reflect.BeanProperty
import com.echoed.chamber.dao.FacebookUserDao
import org.slf4j.LoggerFactory


class FacebookServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[FacebookServiceCreatorActor])

    @BeanProperty var facebookAccess: FacebookAccess = null
    @BeanProperty var facebookUserDao: FacebookUserDao = null

    def receive = {
        case ("code", code: String) => {
            logger.debug("Creating FacebookService using code {}", code)
            val facebookUser = for {
                accessToken: String <- facebookAccess.getAccessToken(code)
                me: FacebookUser <- facebookAccess.getMe(accessToken)
            } yield facebookUserDao.insertOrUpdateFacebookUser(me)

            logger.debug("Creating FacebookService with user {}", facebookUser)

            self.channel ! new FacebookServiceActorClient(Actor.actorOf(
                new FacebookServiceActor(facebookUser.get, facebookAccess, facebookUserDao)).start)

            logger.debug("Created FacebookService with user {}", facebookUser)
        }
    }
}