package com.echoed.chamber.services.twitter

import akka.actor.Actor
import twitter4j.auth.{RequestToken, AccessToken}
import com.echoed.chamber.dao.{TwitterStatusDao, TwitterUserDao}
import com.echoed.chamber.domain.{TwitterUser, TwitterStatus, TwitterFollower}
import org.slf4j.LoggerFactory


class TwitterServiceActor(twitterAccess: TwitterAccess,
                          twitterUserDao: TwitterUserDao,
                          twitterStatusDao: TwitterStatusDao,
                          requestToken: RequestToken,
                          twitterUser: TwitterUser) extends Actor {

      private final val logger = LoggerFactory.getLogger(classOf[TwitterServiceActor])

      def this(twitterAccess:TwitterAccess,twitterUserDao:TwitterUserDao,twitterStatusDao:TwitterStatusDao,requestToken:RequestToken) = this(twitterAccess,twitterUserDao,twitterStatusDao,requestToken,null)
      def this(twitterAccess:TwitterAccess,twitterUserDao:TwitterUserDao,twitterStatusDao:TwitterStatusDao,twitterUser:TwitterUser) = this(twitterAccess,twitterUserDao,twitterStatusDao,null, twitterUser)

      def receive = {
        case ("getRequestToken") =>{
          self.channel ! this.requestToken
        }

        case ("getAccessToken",oAuthVerifier:String) =>{
          val channel = self.channel
          twitterAccess.getAccessToken(requestToken,oAuthVerifier).map { accessToken =>
              channel ! accessToken
          }
        }

        case("getUser") =>{
          self.channel ! twitterAccess.getUser(twitterUser.accessToken,twitterUser.accessTokenSecret,twitterUser.id.toLong).get.asInstanceOf[TwitterUser]
        }

        case("getTwitterUser") =>{
          self.channel ! twitterUser
        }

        case("getFollowers") =>{
          self.channel ! twitterAccess.getFollowers(twitterUser.accessToken, twitterUser.accessTokenSecret, twitterUser.id.toLong).get.asInstanceOf[Array[TwitterFollower]]
        }

        case("assignEchoedUserId", echoedUserId:String) =>{
          twitterUser.echoedUserId = echoedUserId
          twitterUserDao.insertOrUpdateTwitterUser(twitterUser)
          self.channel ! twitterUser
        }
        case ("updateStatus", status:String) => {
          val twitterStatus = twitterAccess.updateStatus(twitterUser.getAccessToken(),twitterUser.getAccessTokenSecret(),status).get.asInstanceOf[TwitterStatus]
          logger.debug("Insert/Updating twitterStatus {}", status)
          twitterStatusDao.insertOrUpdate(twitterStatus)
          self.channel ! twitterStatus
        }

        case("getStatus",statusId:String) =>{
          val twitterStatus = twitterAccess.getStatus(twitterUser.getAccessToken(),twitterUser.getAccessTokenSecret(),statusId).get.asInstanceOf[TwitterStatus]
          logger.debug("Insert/Updating TwitterStatus with statusId {}", statusId)
          twitterStatusDao.insertOrUpdate(twitterStatus)
          self.channel ! twitterStatus
        }

        case("getRetweetIds",tweetId:String) => {
          val retweetList = twitterAccess.getRetweetIds(twitterUser.accessToken, twitterUser.accessTokenSecret,tweetId)
          logger.debug("Received Retweets for tweetId {}",tweetId)
          self.channel ! retweetList
        }

     }
}
