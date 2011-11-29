package com.echoed.chamber.services.twitter

import akka.actor.Actor
import twitter4j.auth.RequestToken
import com.echoed.chamber.dao.{TwitterStatusDao, TwitterUserDao}
import com.echoed.chamber.domain.{TwitterUser, TwitterFollower,Echo}
import org.slf4j.LoggerFactory


class TwitterServiceActor(twitterAccess: TwitterAccess,
                          twitterUserDao: TwitterUserDao,
                          twitterStatusDao: TwitterStatusDao,
                          requestToken: RequestToken,
                          twitterUser: TwitterUser) extends Actor {

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
            self.channel ! twitterAccess.getUser(twitterUser.accessToken, twitterUser.accessTokenSecret, twitterUser.id.toLong).mapTo[TwitterUser]
        }

        case ("getTwitterUser") => {
            self.channel ! twitterUser
        }

        case ("getFollowers") => {
            self.channel ! twitterAccess.getFollowers(twitterUser.accessToken, twitterUser.accessTokenSecret, twitterUser.id.toLong).mapTo[Array[TwitterFollower]]
        }

        case ("assignEchoedUserId", echoedUserId: String) => {
            twitterUser.echoedUserId = echoedUserId
            twitterUserDao.insertOrUpdateTwitterUser(twitterUser)
            self.channel ! twitterUser
        }
        case ("updateStatus", status: String) => {
            val channel = self.channel
            twitterAccess.updateStatus(twitterUser.getAccessToken(), twitterUser.getAccessTokenSecret(), status).map{
                twitterStatus =>
                    logger.debug("Insert/Updating twitterStatus {}", status)
                    twitterStatusDao.insertOrUpdate(_)
                    channel ! twitterStatus
            }
        }

        case ("getStatus", statusId: String) => {
            val channel = self.channel
            twitterAccess.getStatus(twitterUser.getAccessToken(), twitterUser.getAccessTokenSecret(), statusId).map{
                twitterStatus =>
                    logger.debug("Insert/Updating TwitterStatus with statusId {}", statusId)
                    twitterStatusDao.insertOrUpdate(twitterStatus)
                    channel ! twitterStatus
            }
        }

        case ("echo", echo:Echo, message:String ) => {
            val channel = self.channel
            val status = message + " " + echo.imageUrl
            twitterAccess.updateStatus(twitterUser.getAccessToken(),twitterUser.getAccessTokenSecret(),status).map{
                twitterStatus =>
                    twitterStatus.echoId = echo.id
                    twitterStatus.echoedUserId = echo.echoedUserId
                    channel ! twitterStatus
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
