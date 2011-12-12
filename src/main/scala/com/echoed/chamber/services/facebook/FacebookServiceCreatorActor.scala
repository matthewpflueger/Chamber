package com.echoed.chamber.services.facebook

import akka.actor.Actor
import com.echoed.chamber.domain.FacebookUser
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import akka.dispatch.Future
import com.echoed.chamber.dao.{FacebookFriendDao, FacebookPostDao, FacebookUserDao}


class FacebookServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[FacebookServiceCreatorActor])

    @BeanProperty var facebookAccess: FacebookAccess = _
    @BeanProperty var facebookUserDao: FacebookUserDao = _
    @BeanProperty var facebookPostDao: FacebookPostDao = _
    @BeanProperty var facebookFriendDao: FacebookFriendDao = _

    def receive = {
        case ("code", code: String, queryString: String) => {
            logger.debug("Creating FacebookService using code {}", code)
            val facebookUser = for {
                accessToken: String <- facebookAccess.getAccessToken(code, queryString)
                me: FacebookUser <- facebookAccess.getMe(accessToken)
                fbUser: FacebookUser <- Future[FacebookUser] { 
                  Option(facebookUserDao.findByFacebookId(me.facebookId)) match {
                    case Some(fbUser2) =>
                      logger.debug("Found Facebook User {}", me.facebookId)
                      fbUser2
                    case None =>
                      logger.debug("No Facebook User {}", me.facebookId)
                      me
                  } 
                }
                inserted: Int <- Future[Int] { facebookUserDao.insertOrUpdate(fbUser) }
            } yield fbUser

            logger.debug("Creating FacebookService with user {}", facebookUser)

            val channel = self.channel
            facebookUser.map { facebookUser =>
                channel ! new FacebookServiceActorClient(Actor.actorOf(
                    new FacebookServiceActor(
                        facebookUser,
                        facebookAccess,
                        facebookUserDao,
                        facebookPostDao,
                        facebookFriendDao)).start)
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
                        facebookPostDao,
                        facebookFriendDao)).start)

        }
    }
}
