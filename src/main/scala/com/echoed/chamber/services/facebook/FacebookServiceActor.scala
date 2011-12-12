package com.echoed.chamber.services.facebook

import akka.actor.Actor
import org.slf4j.LoggerFactory
import java.util.{UUID, Date}
import com.echoed.chamber.domain._
import com.echoed.chamber.dao.{FacebookFriendDao, FacebookPostDao, FacebookUserDao}
import scala.collection.JavaConversions.asScalaBuffer
import akka.dispatch.Future


class FacebookServiceActor(
        var facebookUser: FacebookUser,
        facebookAccess: FacebookAccess,
        facebookUserDao: FacebookUserDao,
        facebookPostDao: FacebookPostDao,
        facebookFriendDao: FacebookFriendDao) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[FacebookServiceActor])


    def receive = {
        case "facebookUser" => self.channel ! facebookUser

        case ("assignEchoedUser", echoedUser: EchoedUser) =>
            logger.debug("Assigning {} to {}", echoedUser, facebookUser)
            facebookUser = facebookUser.copy(echoedUserId = echoedUser.id)
            facebookUserDao.updateEchoedUser(facebookUser)
            self.channel ! facebookUser

        case ("echo", echo: Echo, message: String) =>
            logger.debug("Creating new FacebookPost with message {} for {}", echo, message)
            val fp = new FacebookPost(
                message,
                echo.imageUrl,
                null,
                facebookUser.id,
                echo.echoedUserId,
                echo.id)
            //TODO externalize the facebookPost url!
            val facebookPost = fp.copy(link = "http://v1-api.echoed.com/echo/%s/%s" format(echo.id, fp.id))
            facebookPostDao.insert(facebookPost)

            val channel = self.channel
            logger.debug("Sending request to post to FacebookAccessActor {}", facebookPost)
            facebookAccess.post(facebookUser.accessToken, facebookUser.facebookId, facebookPost).map[FacebookPost] { fp: FacebookPost =>
                logger.debug("Received post from FacebookAccessActor {}", fp)
                val fpp = fp.copy(postedOn = new Date)
                facebookPostDao.updatePostedOn(fpp)
                channel ! fpp
                logger.debug("Successfully posted {}", fpp)
                fpp
            }

        case 'getFacebookFriends =>
            val channel = self.channel
            Future {
                channel ! asScalaBuffer(facebookFriendDao.findByFacebookUserId(facebookUser.id)).toList
            }

        case '_fetchFacebookFriends =>
            val channel = self.channel
            facebookAccess.getFriends(facebookUser.accessToken, facebookUser.facebookId, facebookUser.id).map[List[FacebookFriend]] {
                facebookFriends: List[FacebookFriend] =>
                    logger.debug("Received from FacebookAccessActor {} FacebookFriends for FacebookUser", facebookFriends.length, facebookUser.id)
                    channel ! facebookFriends
                    facebookFriends.foreach { ff =>
                        facebookFriendDao.insertOrUpdate(ff)
                    }
                    logger.debug("Successfully saved list of {} FacebookFriends", facebookFriends.length)
                    facebookFriends
            }

    }


}
