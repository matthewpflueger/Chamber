package com.echoed.chamber.services.twitter

import twitter4j.auth.RequestToken
import com.echoed.chamber.dao.{TwitterStatusDao, TwitterUserDao}
import java.util.Date
import com.echoed.chamber.domain._
import akka.event.Logging
import akka.util.duration._
import akka.actor.{PoisonPill, ActorRef, Actor}


class TwitterServiceActor(twitterAccess: TwitterAccess,
                          twitterUserDao: TwitterUserDao,
                          twitterStatusDao: TwitterStatusDao,
                          requestToken: RequestToken,
                          echoClickUrl: String,
                          var twitterUser: TwitterUser) extends Actor {

    private final val logger = Logging(context.system, this)

    def this(
            twitterAccess: TwitterAccess,
            twitterUserDao: TwitterUserDao,
            twitterStatusDao: TwitterStatusDao,
            requestToken: RequestToken,
            echoClickUrl: String) = this (
        twitterAccess,
        twitterUserDao,
        twitterStatusDao,
        requestToken,
        echoClickUrl,
        null)

    def this(
            twitterAccess: TwitterAccess,
            twitterUserDao: TwitterUserDao,
            twitterStatusDao: TwitterStatusDao,
            echoClickUrl: String,
            twitterUser: TwitterUser) = this (
        twitterAccess,
        twitterUserDao,
        twitterStatusDao,
        null,
        echoClickUrl,
        twitterUser)


    val maxUpdateTwitterStatusRetries = 10
    val updateTwitterStatusRetryInterval = 60000


    private def updateTwitterStatus(ts: TwitterStatus, me: ActorRef, retries: Int = 0) {

        def scheduleRetry(e: Throwable) {
            if (retries >= maxUpdateTwitterStatusRetries) {
                logger.error("Failed to update Twitter status {}, retries {}, last error was {}", ts, retries, e)
            } else {
                logger.debug("Retrying {} due to error {}", ts, e)
                context.system.scheduler.scheduleOnce(updateTwitterStatusRetryInterval milliseconds, me, RetryTweet(ts, retries + 1))
            }
        }

        logger.debug("Trying to update Twitter status {}, retries {}", ts, retries)
        twitterAccess.updateStatus(
                twitterUser.accessToken,
                twitterUser.accessTokenSecret,
                ts).onComplete(_.fold(
            scheduleRetry(_),
            _ match {
                case UpdateStatusResponse(_, Left(e)) => scheduleRetry(e)
                case UpdateStatusResponse(_, Right(twitterStatus)) =>
                    val tw = twitterStatus.copy(postedOn = new Date)
                    twitterStatusDao.updatePostedOn(tw)
                    logger.debug("Successfully tweeted {}", tw)
            }))
    }

    def receive = {
        case msg: GetRequestToken =>
            sender ! GetRequestTokenResponse(msg, Right(requestToken))


        case msg @ GetAccessToken(oAuthVerifier: String) =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetAccessTokenResponse(msg, Left(TwitterException("Could not get access token", e)))
                logger.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                twitterAccess.getAccessToken(requestToken, oAuthVerifier).onSuccess {
                    case GetAccessTokenForRequestTokenResponse(_, Left(e)) => error(e)
                    case GetAccessTokenForRequestTokenResponse(_, Right(accessToken)) =>
                        channel ! GetAccessTokenResponse(msg, Right(accessToken))
                }.onFailure { case e => error(e) }
            } catch { case e => error(e) }


        case msg: GetUser =>
            sender ! GetUserResponse(msg, Right(twitterUser))


        case msg: GetFollowers =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetFollowersResponse(msg, Left(TwitterException("Could not get Twitter followers", e)))
                logger.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                twitterAccess.getFollowers(
                        twitterUser.accessToken,
                        twitterUser.accessTokenSecret,
                        twitterUser.id,
                        twitterUser.twitterId.toLong).onSuccess {
                    case FetchFollowersResponse(_, Left(e)) => error(e)
                    case FetchFollowersResponse(_, Right(twitterFollowers)) =>
                        channel ! GetFollowersResponse(msg, Right(twitterFollowers))
                }.onFailure { case e => error(e) }
            } catch { case e => error(e) }


        case msg @ AssignEchoedUser(echoedUserId) =>
            twitterUser = twitterUser.copy(echoedUserId = echoedUserId)
            sender ! AssignEchoedUserResponse(msg, Right(twitterUser))
            twitterUserDao.updateEchoedUser(twitterUser)


        case msg @ Tweet(echo, message) =>
            val me = self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! TweetResponse(msg, Left(TwitterException("Could not tweet", e)))
                logger.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                logger.debug("Creating new TwitterStatus with message {} for {}", echo, message)
                var status = "%s %s/" format(message.take(115), echoClickUrl)
                var twitterStatus = new TwitterStatus(
                    echo.id,
                    echo.echoedUserId,
                    status)
                status = status + twitterStatus.id
                twitterStatus = twitterStatus.copy(message = status)
                twitterStatusDao.insert(twitterStatus)
                channel ! TweetResponse(msg, Right(twitterStatus))
                updateTwitterStatus(twitterStatus, me)
            } catch { case e => error(e) }

        case RetryTweet(tweet, retries) =>
            updateTwitterStatus(tweet, self, retries + 1)

        case msg @ Logout(twitterUserId) =>
            val channel = context.sender

            try {
                assert(twitterUser.id == twitterUserId)
                channel ! LogoutResponse(msg, Right(true))
                twitterAccess.logout(twitterUser.accessToken)
                self ! PoisonPill
                logger.debug("Logged out Twitter user {}", twitterUserId)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(TwitterException("Could not logout of Twitter", e)))
                    logger.error("Unexpected error processing {}: {}", msg, e)
            }
    }
}
