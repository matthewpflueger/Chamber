package com.echoed.chamber.services.facebook

import java.util.Date
import com.echoed.chamber.domain._
import com.echoed.chamber.dao.{FacebookFriendDao, FacebookPostDao, FacebookUserDao}
import scala.collection.JavaConversions.asScalaBuffer
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.actor.{PoisonPill, ActorRef, Actor}


class FacebookServiceActor(
        var facebookUser: FacebookUser,
        facebookAccess: FacebookAccess,
        facebookUserDao: FacebookUserDao,
        facebookPostDao: FacebookPostDao,
        facebookFriendDao: FacebookFriendDao,
        echoClickUrl: String) extends Actor {

    private final val logger = Logging(context.system, this)

    val maxPostToFacebookRetries = 10
    val facebookPostRetryInterval = 60000


    override def preStart {
        //hack to reset our posts to be crawled - really should send a message to FacebookPostCrawler to crawl our posts...
        facebookPostDao.resetPostsToCrawl(facebookUser.id)
    }


    private def postToFacebook(fp: FacebookPost, me: ActorRef, retries: Int = 0) {

        def scheduleRetry(e: Throwable) {
            if (retries >= maxPostToFacebookRetries) {
                logger.error("Failed to post {}, retries {}, last error was {}", fp, retries, e)
            } else {
                logger.debug("Retrying {} due to error {}", fp, e)
                context.system.scheduler.scheduleOnce(facebookPostRetryInterval milliseconds, me, RetryEchoToFacebook(fp, retries + 1))
            }
        }

        logger.debug("Trying to post {}, retries {}", fp, retries)
        facebookAccess.post(facebookUser.accessToken, facebookUser.facebookId, fp).onComplete(_.fold(
            scheduleRetry(_),
            _ match {
                case PostResponse(_, Left(e)) => scheduleRetry(e)
                case PostResponse(_, Right(fp)) =>
                    logger.debug("Successful post {}, retries {}", fp, retries)
                    facebookPostDao.updatePostedOn(fp.copy(postedOn = new Date))
            }))
    }


    def receive = {
        case msg: GetFacebookUser =>
            val channel = context.sender
            channel ! GetFacebookUserResponse(msg, Right(facebookUser))

        case msg @ AssignEchoedUser(echoedUser) =>
            val channel = context.sender

            try {
                logger.debug("Assigning {} to {}", echoedUser, facebookUser)
                facebookUser = facebookUser.copy(echoedUserId = echoedUser.id)
                facebookUserDao.updateEchoedUser(facebookUser)
                channel ! AssignEchoedUserResponse(msg, Right(facebookUser))
            } catch {
                case e =>
                    channel ! AssignEchoedUserResponse(msg, Left(FacebookException("Could not assign Echoed user", e)))
                    logger.error("Error processing {}: {}", msg, e)
            }


        case msg @ EchoToFacebook(echo, message) =>
            val me = self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! EchoToFacebookResponse(msg, Left(FacebookException("Could not post to Facebook", e)))
                logger.error("Error processing {}: {}", msg, e)
            }

            try {
                logger.debug("Creating new FacebookPost with message {} for {}", echo, message)
                val name: String = echo.productName + " from " + echo.brand + "<center></center>"
                val caption: String = echo.brand + "<center></center>" + echo.productName
                var fp = new FacebookPost(
                        name,
                        message.take(254),
                        caption,
                        echo.imageUrl,
                        null,
                        facebookUser.id,
                        echo.echoedUserId,
                        echo.id)
                fp = fp.copy(link = "%s/%s" format(echoClickUrl, fp.id))
                facebookPostDao.insert(fp)
                channel ! EchoToFacebookResponse(msg, Right(fp))
                postToFacebook(fp, me)
            } catch { case e => error(e) }

        case msg @ PublishActionToFacebook(action, obj, objUrl) =>
            val me = self
            val channel = context.sender
            def error(e: Throwable) {
                channel ! PublishActionToFacebookResponse(msg, Left(FacebookException("Could not publish action to Facebook", e)))
                logger.error("Error processing {}: {}", msg, e)
            }
            
            try {
                logger.debug("Publishing Action %s?%s=%s for facebookUser: {}" format (action, obj, objUrl) , facebookUser)
                facebookAccess.publishAction(facebookUser.accessToken, action, obj, objUrl ).onComplete(_.fold(
                    error(_),
                    _ match {
                        case PublishActionResponse(_, Left(e)) => error(e)
                        case PublishActionResponse(_, Right(facebookAction)) => 
                            channel ! PublishActionToFacebookResponse(msg, Right(facebookAction))
                            logger.debug("Successfully published action to Facebook")
                    }))
                
            } catch {
                case e =>
                    logger.error("Error processing {}: {}", msg, e)
                    error(e)
            }

        case RetryEchoToFacebook(facebookPost, retries) =>
            postToFacebook(facebookPost, self, retries + 1)


        case msg: GetFriends =>
            val channel = context.sender

            try {
                channel ! GetFriendsResponse(
                        msg,
                        Right(asScalaBuffer(facebookFriendDao.findByFacebookUserId(facebookUser.id)).toList))
            } catch {
                case e =>
                    logger.error("Error processing {}: {}", msg, e)
                    channel ! GetFriendsResponse(msg, Left(FacebookException("Cannot get Facebook friends", e)))
            }

        case '_fetchFacebookFriends =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetFriendsResponse(
                        GetFriends(facebookUser.accessToken, facebookUser.facebookId, facebookUser.id),
                        Left(FacebookException("Could not fetch friends", e)))
                logger.error("Error fetching Facebook friends for {}: {}", facebookUser.id, e)
            }

            try {
                logger.debug("Fetching friends for FacebookUser {}", facebookUser.id)
                facebookAccess.getFriends(facebookUser.accessToken, facebookUser.facebookId, facebookUser.id).onComplete(_.fold(
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
            val channel = context.sender

            try {
                assert(facebookUser.id == facebookUserId)
                facebookAccess.logout(facebookUser.accessToken)
                channel ! LogoutResponse(msg, Right(true))
                self ! PoisonPill
                logger.debug("Logged out {}", facebookUser)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(FacebookException("Could not logout of Facebook", e)))
                    logger.error("Error processing {}: {}", msg, e)
            }

        case msg @ UpdateAccessToken(accessToken) =>
            val channel = context.sender

            try {
                facebookUser = facebookUser.copy(accessToken = accessToken)
                facebookUserDao.updateAccessToken(facebookUser)
                channel ! UpdateAccessTokenResponse(msg, Right(facebookUser))
            } catch {
                case e =>
                    channel ! UpdateAccessTokenResponse(msg, Left(FacebookException("Could not update access token", e)))
                    logger.error("Error processing {}: {}", msg, e)
            }
    }


}
