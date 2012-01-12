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
        case msg @ FetchRequestToken(callbackUrl) =>
            try {
                val twitterHandler = getTwitterHandler(null)
                val requestToken = twitterHandler.getOAuthRequestToken(callbackUrl)
                val key = "requestToken:" + requestToken.getToken
                cache += (key -> twitterHandler)
                self.channel ! FetchRequestTokenResponse(msg, Right(requestToken))
            } catch {
                case e =>
                    self.channel ! FetchRequestTokenResponse(msg, Left(TwitterException("Cannot get request token", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ GetAccessTokenForRequestToken(requestToken, oAuthVerifier) =>
            try {
                val twitterHandler = getTwitterHandler("requestToken:%s" format requestToken.getToken)
                val accessToken = twitterHandler.getOAuthAccessToken(requestToken, oAuthVerifier)
                self.channel ! GetAccessTokenForRequestTokenResponse(msg, Right(accessToken))
            } catch {
                case e =>
                    self.channel ! GetAccessTokenForRequestTokenResponse(
                            msg,
                            Left(TwitterException("Cannot get access token", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ FetchAccessToken(accessToken, accessTokenSecret) =>
            try {
                val key = "accessToken:" + accessToken
                val twitterHandler = getTwitterHandler(key, accessToken, accessTokenSecret)
                self.channel ! FetchAccessTokenResponse(msg, Right(twitterHandler.getOAuthAccessToken()))
            } catch {
                case e =>
                    self.channel ! FetchAccessTokenResponse(msg, Left(TwitterException("Cannot get access token", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ FetchUser(accessToken, accessTokenSecret, userId) =>
            try {
                val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
                val user: User = twitterHandler.showUser(userId)
                val twitterUser: TwitterUser = new TwitterUser(
                        twitterId = user.getId.toString,
                        screenName = user.getScreenName,
                        name = user.getName,
                        profileImageUrl = user.getProfileImageURL.toExternalForm,
                        location = user.getLocation,
                        timezone = user.getTimeZone,
                        accessToken = accessToken,
                        accessTokenSecret = accessTokenSecret)
                self.channel ! FetchUserResponse(msg, Right(twitterUser))
            } catch {
                case e =>
                    self.channel ! FetchUserResponse(msg, Left(TwitterException("Cannot get user", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ FetchFollowers(accessToken, accessTokenSecret, twitterUserId, twitterId) =>
            try {
                logger.debug("Attempting to get Twitter followers for {}", twitterId)
                val ids = getTwitterHandler(accessToken, accessTokenSecret).getFollowersIDs(twitterId, -1).getIDs
                logger.debug("Twitter user {} has {} followers", twitterUserId, ids.length)
                val twitterFollowers = ids.map( id => new TwitterFollower(twitterUserId, id.toString, null)).toList
                logger.debug("Successfully created {} TwitterFollwers", twitterFollowers.size)
                self.channel ! FetchFollowersResponse(msg, Right(twitterFollowers))
            } catch {
                case e =>
                    self.channel ! FetchFollowersResponse(msg, Left(TwitterException("Cannot get Twitter followers", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ UpdateStatus(accessToken, accessTokenSecret, twitterStatus) =>
            try {
                val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
                val status = twitterHandler.updateStatus(twitterStatus.message)
                self.channel ! UpdateStatusResponse(msg, Right(twitterStatus.copy(
                        twitterId = status.getId.toString,
                        createdAt = status.getCreatedAt,
                        text = status.getText,
                        source = status.getSource)))
            } catch {
                case e =>
                    self.channel ! UpdateStatusResponse(msg, Left(TwitterException("Cannot update Twitter status", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
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

            new TwitterFactory(configurationBuilder.build()).getInstance()
        })
    }
}
