package com.echoed.chamber.services.facebook

import akka.actor.Actor
import com.echoed.chamber.domain.{FacebookPost, Echo, EchoedUser, FacebookUser}
import com.echoed.chamber.dao.{FacebookPostDao, FacebookUserDao}
import org.slf4j.LoggerFactory
import java.util.{UUID, Date}


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
                val uuid = UUID.randomUUID().toString
                val facebookPost = new FacebookPost(
                    uuid,
                    message,
                    echo.imageUrl,
                    //TODO externalize this!
                    "http://v1-api.echoed.com/echo/%s/%s" format(echo.id, uuid),
                    facebookUser.id,
                    echo.echoedUserId,
                    echo.id)
                facebookPostDao.insert(facebookPost)

                val channel = self.channel
                logger.debug("Sending request to post to FacebookAccessActor {}", facebookPost)
                facebookAccess.post(facebookUser.accessToken, facebookUser.id, facebookPost).map[FacebookPost] { fp: FacebookPost =>
                    logger.debug("Received post from FacebookAccessActor {}", fp)
                    fp.postedOn = new Date
                    facebookPostDao.updatePostedOn(fp)
                    channel ! fp
                    logger.debug("Successfully posted {}", fp)
                    fp
                }


    }
}
