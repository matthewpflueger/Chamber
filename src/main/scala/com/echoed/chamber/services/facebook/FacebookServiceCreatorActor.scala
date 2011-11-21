package com.echoed.chamber.services.facebook

import akka.actor.Actor
import com.echoed.chamber.domain.FacebookUser
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import akka.dispatch.Future
import com.echoed.chamber.dao.{FacebookPostDao, FacebookUserDao}


class FacebookServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[FacebookServiceCreatorActor])

    @BeanProperty var facebookAccess: FacebookAccess = _
    @BeanProperty var facebookUserDao: FacebookUserDao = _
    @BeanProperty var facebookPostDao: FacebookPostDao = _

    def receive = {
        case ("code", code: String) => {
            logger.debug("Creating FacebookService using code {}", code)
            val facebookUser = for {
                accessToken: String <- facebookAccess.getAccessToken(code)
                me: FacebookUser <- facebookAccess.getMe(accessToken)
                inserted: Int <- Future[Int] { facebookUserDao.insertOrUpdate(me) }
            } yield me

            logger.debug("Creating FacebookService with user {}", facebookUser)

            val channel = self.channel
            facebookUser.map { facebookUser =>
                channel ! new FacebookServiceActorClient(Actor.actorOf(
                    new FacebookServiceActor(
                        facebookUser,
                        facebookAccess,
                        facebookUserDao,
                        facebookPostDao)).start)
            }

            logger.debug("Created FacebookService with user {}", facebookUser)
        }
        case ("facebookUserId", facebookUserId: String) => {
            logger.debug("Creating FacebookService using facebookUserId {}", facebookUserId)
            val facebookUser = Option(facebookUserDao.findById(facebookUserId))
            self.channel ! new FacebookServiceActorClient(Actor.actorOf(
                new FacebookServiceActor(
                        facebookUser.get,
                        facebookAccess,
                        facebookUserDao,
                        facebookPostDao)).start)

        }
    }
}
