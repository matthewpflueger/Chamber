package com.echoed.chamber.services.twitter

import akka.actor.Actor
import twitter4j.auth.RequestToken
import com.echoed.chamber.dao.{TwitterStatusDao, TwitterUserDao}
import org.slf4j.LoggerFactory
import java.util.Date
import com.echoed.chamber.domain._


class TwitterServiceActor(twitterAccess: TwitterAccess,
                          twitterUserDao: TwitterUserDao,
                          twitterStatusDao: TwitterStatusDao,
                          requestToken: RequestToken,
                          var twitterUser: TwitterUser) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[TwitterServiceActor])

    def this(twitterAccess: TwitterAccess, twitterUserDao: TwitterUserDao, twitterStatusDao: TwitterStatusDao, requestToken: RequestToken) = this (twitterAccess, twitterUserDao, twitterStatusDao, requestToken, null)

    def this(twitterAccess: TwitterAccess, twitterUserDao: TwitterUserDao, twitterStatusDao: TwitterStatusDao, twitterUser: TwitterUser) = this (twitterAccess, twitterUserDao, twitterStatusDao, null, twitterUser)

    def receive = {
        case ("getRequestToken") => {
            self.channel ! this.requestToken
        }

        case ("getAccessToken", oAuthVerifier: String) => {
            val channel = self.channel
            twitterAccess.getAccessToken(requestToken, oAuthVerifier).map {
                accessToken =>
                    channel ! accessToken
            }
        }

        case ("getUser") => {
            self.channel ! twitterAccess.getUser(twitterUser.accessToken, twitterUser.accessTokenSecret, twitterUser.twitterId.toLong).mapTo[TwitterUser]
        }

        case ("getTwitterUser") => {
            self.channel ! twitterUser
        }

        case ("getFollowers") =>
            val channel = self.channel
            twitterAccess.getFollowers(
                    twitterUser.accessToken,
                    twitterUser.accessTokenSecret,
                    twitterUser.id,
                    twitterUser.twitterId.toLong).onComplete(_.value.get.fold(
                        e => channel ! e,
                        followers => channel ! followers
                    ))

        case ("assignEchoedUserId", echoedUserId: String) => {
            twitterUser = twitterUser.copy(echoedUserId = echoedUserId)
            twitterUserDao.updateEchoedUser(twitterUser)
            self.channel ! twitterUser
        }

        case ("getStatus", statusId: String) => {
            val channel = self.channel
            twitterAccess.getStatus(twitterUser.accessToken, twitterUser.accessTokenSecret, statusId).map{
                twitterStatus =>
                    logger.debug("Insert/Updating TwitterStatus with statusId {}", statusId)
                    //twitterStatusDao.insertOrUpdate(twitterStatus)
                    channel ! twitterStatus
            }
        }

        case ("echo", echo:Echo, message:String ) => {
            logger.debug("Creating new TwitterStatus with message {} for {}", echo, message)
            var status = message + " http://v1-api.echoed.com/echo/" + echo.id + "/"
            var twitterStatus = new TwitterStatus(
                echo.id,
                echo.echoedUserId,
                status)
            status = status + twitterStatus.id
            twitterStatus = twitterStatus.copy(message = status)
            twitterStatusDao.insert(twitterStatus)

            val channel = self.channel
            twitterAccess.updateStatus(twitterUser.accessToken,twitterUser.accessTokenSecret,twitterStatus).map{ twitterStatus =>
                logger.debug("Received from TwitterAccessActor {}", twitterStatus)
                val tw = twitterStatus.copy(postedOn = new Date)
                twitterStatusDao.updatePostedOn(tw)
                channel ! tw
                logger.debug("Successfully posted {}", tw)
            }
        }

        case ("getRetweetIds", tweetId: String) => {
            val channel = self.channel
            twitterAccess.getRetweetIds(twitterUser.accessToken, twitterUser.accessTokenSecret, tweetId).map{
                retweetList =>
                    logger.debug("Received Retweets for tweetId {}", tweetId)
                    channel ! retweetList
            }
        }

    }
}
