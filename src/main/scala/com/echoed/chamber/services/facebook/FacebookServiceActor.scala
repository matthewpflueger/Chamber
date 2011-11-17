package com.echoed.chamber.services.facebook

import akka.actor.Actor
import com.echoed.chamber.domain.{FacebookPost, Echo, EchoedUser, FacebookUser}
import com.echoed.chamber.dao.{FacebookPostDao, FacebookUserDao}
import org.slf4j.LoggerFactory
import java.util.Date


class FacebookServiceActor(
        facebookUser: FacebookUser,
        facebookAccess: FacebookAccess,
        facebookUserDao: FacebookUserDao,
        facebookPostDao: FacebookPostDao) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[FacebookServiceActor])

    def receive = {
        case "facebookUser" => self.channel ! facebookUser

        case ("assignEchoedUser", echoedUser: EchoedUser) =>
                logger.debug("Assigning {} to {}", echoedUser, facebookUser)
                facebookUser.echoedUserId = echoedUser.id
                facebookUserDao.insertOrUpdate(facebookUser)
                self.channel ! facebookUser

        case ("echo", echo: Echo, message: String) =>
                logger.debug("Creating new FacebookPost with message {} for {}", echo, message)
                val facebookPost = new FacebookPost(
                    null,
                    message,
                    echo.imageUrl,
                    //TODO externalize this!
                    "http://v1-api.echoed.com/echoes/" + echo.id,
                    facebookUser.id,
                    echo.echoedUserId,
                    echo.id,
                    null,
                    new Date,
                    null)
                facebookPostDao.insert(facebookPost)

                val channel = self.channel
                facebookAccess.post(facebookUser.accessToken, facebookUser.id, facebookPost) map { facebookPost =>
                    facebookPost.postedOn = new Date
                    facebookPostDao.updatePostedOn(facebookPost)
                    logger.debug("Successfully posted {}", facebookPost)
                    channel ! facebookPost
                }


    }
}
