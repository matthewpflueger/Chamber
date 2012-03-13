package com.echoed.chamber.services.facebook

import org.slf4j.LoggerFactory
import java.util.Date
import com.echoed.chamber.domain._
import com.echoed.chamber.dao.{FacebookFriendDao, FacebookPostDao, FacebookUserDao, RetailerDao,  RetailerSettingsDao}
import scala.collection.JavaConversions.asScalaBuffer
import akka.actor.{Channel, Actor}


class FacebookServiceActor(
        var facebookUser: FacebookUser,
        facebookAccess: FacebookAccess,
        facebookUserDao: FacebookUserDao,
        facebookPostDao: FacebookPostDao,
        facebookFriendDao: FacebookFriendDao,
        retailerDao: RetailerDao,
        retailerSettingsDao: RetailerSettingsDao,
        echoClickUrl: String) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[FacebookServiceActor])

    self.id = "FacebookService:%s" format facebookUser.id

    def receive = {
        case msg: GetFacebookUser =>
            val channel: Channel[GetFacebookUserResponse] = self.channel
            channel ! GetFacebookUserResponse(msg, Right(facebookUser))

        case msg @ AssignEchoedUser(echoedUser) =>
            val channel: Channel[AssignEchoedUserResponse] = self.channel

            try {
                logger.debug("Assigning {} to {}", echoedUser, facebookUser)
                facebookUser = facebookUser.copy(echoedUserId = echoedUser.id)
                facebookUserDao.updateEchoedUser(facebookUser)
                channel ! AssignEchoedUserResponse(msg, Right(facebookUser))
            } catch {
                case e =>
                    channel ! AssignEchoedUserResponse(msg, Left(FacebookException("Could not assign Echoed user", e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ EchoToFacebook(echo, message) =>
            val channel: Channel[EchoToFacebookResponse] = self.channel

            def error(e: Throwable) {
                channel ! EchoToFacebookResponse(msg, Left(FacebookException("Could not post to Facebook", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating new FacebookPost with message {} for {}", echo, message)
                logger.debug("Retailer Settings ID {}", echo.retailerSettingsId)
                val retailerSettings = retailerSettingsDao.findById(echo.retailerSettingsId)
                val retailer = retailerDao.findById(echo.retailerId)
                val name = "Buy anything now at " + retailer.name + " and  receive up to " + "%1.0f".format(retailerSettings.maxPercentage*100)  + "% Cash Back when you share it with Echoed!"
                val caption: String = echo.brand + "<center></center>" + echo.productName
                val fp = new FacebookPost(
                        name,
                        message,
                        caption,
                        echo.imageUrl,
                        null,
                        facebookUser.id,
                        echo.echoedUserId,
                        echo.id)
                //TODO externalize the facebookPost url!
                val facebookPost = fp.copy(link = "%s/%s/%s" format(echoClickUrl, echo.id, fp.id))
                facebookPostDao.insert(facebookPost)

                logger.debug("Sending request to post to FacebookAccessActor {}", facebookPost)
                facebookAccess.post(facebookUser.accessToken, facebookUser.facebookId, facebookPost).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case PostResponse(_, Left(e: FacebookException)) => channel ! EchoToFacebookResponse(msg, Left(e))
                        case PostResponse(_, Left(e)) => error(e)
                        case PostResponse(_, Right(fp)) =>
                                logger.debug("Received post from FacebookAccessActor {}", fp)
                                val fpp = fp.copy(postedOn = new Date)
                                channel ! EchoToFacebookResponse(msg, Right(fp))
                                facebookPostDao.updatePostedOn(fpp)
                                logger.debug("Successfully posted {}", fpp)
                    }))
            } catch { case e => error(e) }


        case msg: GetFriends =>
            val channel: Channel[GetFriendsResponse] = self.channel

            try {
                channel ! GetFriendsResponse(
                        msg,
                        Right(asScalaBuffer(facebookFriendDao.findByFacebookUserId(facebookUser.id)).toList))
            } catch {
                case e =>
                    logger.error("Error processing %s" format msg, e)
                    channel ! GetFriendsResponse(msg, Left(FacebookException("Cannot get Facebook friends", e)))
            }

        case '_fetchFacebookFriends =>
            val channel = self.channel

            def error(e: Throwable) {
                channel ! GetFriendsResponse(
                        GetFriends(facebookUser.accessToken, facebookUser.facebookId, facebookUser.id),
                        Left(FacebookException("Could not fetch friends", e)))
                logger.error("Error fetching Facebook friends for %s" format facebookUser.id, e)
            }

            try {
                logger.debug("Fetching friends for FacebookUser {}", facebookUser.id)
                facebookAccess.getFriends(facebookUser.accessToken, facebookUser.facebookId, facebookUser.id).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case msg @ GetFriendsResponse(_, Right(facebookFriends)) =>
                            logger.debug("Received from FacebookAccessActor {} FacebookFriends for FacebookUser {}",
                                    facebookFriends.length,
                                    facebookUser.id)
                            channel ! msg
                            facebookFriends.foreach(facebookFriendDao.insertOrUpdate(_))
                            logger.debug("Successfully saved list of {} FacebookFriends for {}", facebookFriends.length, facebookUser.id)
                            facebookFriends

                        case msg @ GetFriendsResponse(_, Left(e)) =>
                            logger.debug("Received error response for fetching friends for FacebookUser {}", facebookUser.id)
                            channel ! msg
                }))
            } catch {
                case e => error(e)
            }

        case msg @ Logout(facebookUserId) =>
            val channel: Channel[LogoutResponse] = self.channel

            try {
                assert(facebookUser.id == facebookUserId)
                facebookAccess.logout(facebookUser.accessToken)
                channel ! LogoutResponse(msg, Right(true))
                self.stop
                logger.debug("Logged out {}", facebookUser)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(FacebookException("Could not logout of Facebook", e)))
                    logger.error("Error processing %s" format msg, e)
            }

        case msg @ UpdateAccessToken(accessToken) =>
            val channel: Channel[UpdateAccessTokenResponse] = self.channel

            try {
                facebookUser = facebookUser.copy(accessToken = accessToken)
                facebookUserDao.updateAccessToken(facebookUser)
                channel ! UpdateAccessTokenResponse(msg, Right(facebookUser))
            } catch {
                case e =>
                    channel ! UpdateAccessTokenResponse(msg, Left(FacebookException("Could not update access token", e)))
                    logger.error("Error processing %s" format msg, e)
            }
    }


}
