package com.echoed.chamber.services.twitter

import akka.actor.Actor
import com.echoed.chamber.domain.{TwitterFollower, TwitterUser, TwitterStatus}
import reflect.BeanProperty
import java.util.Properties
import org.slf4j.LoggerFactory
import twitter4j.auth.{RequestToken,AccessToken}
import twitter4j.{TwitterFactory, Twitter}
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{Status,User}
import scala.collection.mutable.{ListBuffer, WeakHashMap}

class TwitterAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[TwitterAccessActor])

    @BeanProperty var consumerKey: String = _ //Called Consumer Key for Twitter
    @BeanProperty var consumerSecret: String = _  //Called Consumer Secret for Twitter
    @BeanProperty var callbackUrl: String = _  //Callback Url

    @BeanProperty var properties: Properties = _

    private val cache = WeakHashMap[String, Twitter]()

    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        // where placeholder values were not being resolved
        {
            consumerKey = properties.getProperty("consumerKey")
            consumerSecret = properties.getProperty("consumerSecret")
            //callbackUrl = properties.getProperty("callbackUrl")
            consumerKey != null && consumerSecret != null //&& callbackUrl != null
        } ensuring(_ == true, "Missing parameters")
    }

    def receive = {

        case ("requestToken", callbackUrl: String) => {
            val twitterHandler = getTwitterHandler(null)
            var requestToken: RequestToken = twitterHandler.getOAuthRequestToken(callbackUrl)
            val key: String = "requestToken:" + requestToken.getToken
            cache += (key -> twitterHandler)
            self.channel ! requestToken
        }

        case ("accessToken", requestToken: RequestToken, oAuthVerifier: String) => {
            var key = "requestToken:" + requestToken.getToken
            val twitterHandler = getTwitterHandler(key)
            var accessToken: AccessToken = twitterHandler.getOAuthAccessToken(requestToken, oAuthVerifier)
            self.channel ! accessToken
        }

        case ("accessToken", accessToken: String, accessTokenSecret: String) => {
            var key = "accessToken:" + accessToken
            val twitterHandler = getTwitterHandler(key, accessToken, accessTokenSecret)
            self.channel ! twitterHandler.getOAuthAccessToken()
        }

        case ("getUser", accessToken: String, accessTokenSecret: String, userId: Long) => {
            val twitterHandler = getTwitterHandler("accessToken:" + accessToken, accessToken, accessTokenSecret)
            val user: User = twitterHandler.showUser(userId)
            val twitterUser: TwitterUser = new TwitterUser(
                    null,
                    user.getId.toString,
                    user.getScreenName,
                    user.getName,
                    user.getProfileImageURL.toExternalForm,
                    user.getLocation,
                    user.getTimeZone,
                    accessToken,
                    accessTokenSecret)

            self.channel ! twitterUser
        }

        case ("getFollowers", accessToken: String, accessTokenSecret: String, twitterUserId: String, twitterId: Long) => {
            logger.debug("Attempting to receive TwitterFollowers for UserId {} ", twitterId)

            val ids = getTwitterHandler(accessToken, accessTokenSecret).getFollowersIDs(twitterId, -1).getIDs

            logger.debug("TwitterUser {} with has {} followers", twitterUserId, ids.length)
            val twitterFollowers = ids.map( id => new TwitterFollower(twitterUserId, id.toString, null)).toList
            logger.debug("Got {} ", twitterFollowers)

            self.channel ! twitterFollowers
        }

        case ("updateStatus", accessToken: String, accessTokenSecret: String, twitterStatus: TwitterStatus) => {
            val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
            val status: Status = twitterHandler.updateStatus(twitterStatus.message)
            self.channel ! twitterStatus.copy(
                    twitterId = status.getId.toString,
                    createdAt = status.getCreatedAt,
                    text = status.getText,
                    source = status.getSource)
        }

//        case ("getStatus", accessToken: String, accessTokenSecret: String, statusId: String) => {
//            val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
//            val status: Status = twitterHandler.showStatus(statusId.toLong)
//            val twitterStatus: TwitterStatus = new TwitterStatus(status) //status.getId.toString, status.getUser.getId.toString, status.getText, status.getCreatedAt, status.getSource, null, null)
//            self.channel ! twitterStatus
//        }

        case ("getRetweets", accessToken: String, accessTokenSecret: String, tweetId: String) => {
            val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
            //val responseList = twitterHandler.getRetweets(tweetId.toLong)
            self.channel ! null
        }
    }

    private def getTwitterHandler(key: String): Twitter = {
        getTwitterHandler(key, null, null)
    }

    private def getTwitterHandler(accessToken: String, accessTokenSecret: String): Twitter =
        getTwitterHandler("accessToken:%s" format accessToken, accessToken, accessTokenSecret)

    private def getTwitterHandler(key: String, accessToken: String, accessTokenSecret: String) = {

        cache.getOrElse(key, {
            val  configurationBuilder: ConfigurationBuilder = new ConfigurationBuilder()

            configurationBuilder.setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .setOAuthAccessToken(accessToken)
                    .setOAuthAccessTokenSecret(accessTokenSecret)

            val twitterHandler: Twitter = new TwitterFactory(configurationBuilder.build()).getInstance()
            twitterHandler
        })
    }
}
