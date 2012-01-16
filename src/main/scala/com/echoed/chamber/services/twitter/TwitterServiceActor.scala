package com.echoed.chamber.services.twitter

import twitter4j.auth.RequestToken
import com.echoed.chamber.dao.{TwitterStatusDao, TwitterUserDao}
import org.slf4j.LoggerFactory
import java.util.Date
import com.echoed.chamber.domain._
import akka.actor.{Channel, Actor}


class TwitterServiceActor(twitterAccess: TwitterAccess,
                          twitterUserDao: TwitterUserDao,
                          twitterStatusDao: TwitterStatusDao,
                          requestToken: RequestToken,
                          var twitterUser: TwitterUser) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[TwitterServiceActor])

    def this(twitterAccess: TwitterAccess, twitterUserDao: TwitterUserDao, twitterStatusDao: TwitterStatusDao, requestToken: RequestToken) = this (twitterAccess, twitterUserDao, twitterStatusDao, requestToken, null)

    def this(twitterAccess: TwitterAccess, twitterUserDao: TwitterUserDao, twitterStatusDao: TwitterStatusDao, twitterUser: TwitterUser) = this (twitterAccess, twitterUserDao, twitterStatusDao, null, twitterUser)

    //FIXME should not ever be null
    self.id = "TwitterService:%s" format Option(twitterUser).map(_.id).getOrElse("NONE")

    def receive = {
        case msg: GetRequestToken =>
            self.channel ! GetRequestTokenResponse(msg, Right(requestToken))


        case msg @ GetAccessToken(oAuthVerifier: String) =>
            val channel: Channel[GetAccessTokenResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetAccessTokenResponse(msg, Left(TwitterException("Could not get access token", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                twitterAccess.getAccessToken(requestToken, oAuthVerifier).onResult {
                    case GetAccessTokenForRequestTokenResponse(_, Left(e)) => error(e)
                    case GetAccessTokenForRequestTokenResponse(_, Right(accessToken)) =>
                        channel ! GetAccessTokenResponse(msg, Right(accessToken))
                }.onException { case e => error(e) }
            } catch { case e => error(e) }


        case msg: GetUser =>
            self.channel ! GetUserResponse(msg, Right(twitterUser))


        case msg: GetFollowers =>
            val channel: Channel[GetFollowersResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetFollowersResponse(msg, Left(TwitterException("Could not get Twitter followers", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                twitterAccess.getFollowers(
                        twitterUser.accessToken,
                        twitterUser.accessTokenSecret,
                        twitterUser.id,
                        twitterUser.twitterId.toLong).onResult {
                    case FetchFollowersResponse(_, Left(e)) => error(e)
                    case FetchFollowersResponse(_, Right(twitterFollowers)) =>
                        channel ! GetFollowersResponse(msg, Right(twitterFollowers))
                }.onException { case e => error(e) }
            } catch { case e => error(e) }


        case msg @ AssignEchoedUser(echoedUserId) =>
            twitterUser = twitterUser.copy(echoedUserId = echoedUserId)
            self.channel ! AssignEchoedUserResponse(msg, Right(twitterUser))
            twitterUserDao.updateEchoedUser(twitterUser)


        case msg @ Tweet(echo, message) =>
            val channel = self.channel

            def error(e: Throwable) {
                channel ! TweetResponse(msg, Left(TwitterException("Could not tweet", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating new TwitterStatus with message {} for {}", echo, message)
                var status = message + " http://v1-api.echoed.com/echo/" + echo.id + "/"
                var twitterStatus = new TwitterStatus(
                    echo.id,
                    echo.echoedUserId,
                    status)
                status = status + twitterStatus.id
                twitterStatus = twitterStatus.copy(message = status)
                twitterStatusDao.insert(twitterStatus)

                twitterAccess.updateStatus(
                        twitterUser.accessToken,
                        twitterUser.accessTokenSecret,
                        twitterStatus).onResult {
                    case UpdateStatusResponse(_, Left(e)) => error(e)
                    case UpdateStatusResponse(_, Right(twitterStatus)) =>
                        val tw = twitterStatus.copy(postedOn = new Date)
                        twitterStatusDao.updatePostedOn(tw)
                        channel ! TweetResponse(msg, Right(tw))
                        logger.debug("Successfully tweeted {}", twitterStatus)
                }.onException { case e => error(e) }
            } catch { case e => error(e) }


        case msg @ Logout(twitterUserId) =>
            val channel: Channel[LogoutResponse] = self.channel

            try {
                assert(twitterUser.id == twitterUserId)
                channel ! LogoutResponse(msg, Right(true))
                twitterAccess.logout(twitterUser.accessToken)
                self.stop
                logger.debug("Logged out Twitter user {}", twitterUserId)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(TwitterException("Could not logout of Twitter", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }
    }
}
