package com.echoed.chamber.services.twitter

import com.echoed.chamber.domain.{TwitterFollower, TwitterUser}
import reflect.BeanProperty
import java.util.Properties
import org.slf4j.LoggerFactory
import twitter4j.{TwitterFactory, Twitter}
import twitter4j.conf.ConfigurationBuilder
import twitter4j.User
import scala.collection.mutable.WeakHashMap
import scalaz._
import Scalaz._
import akka.actor.{Channel, Actor}
import twitter4j.auth.RequestToken
import java.util.concurrent.{ConcurrentHashMap => JConcurrentHashMap}
import scala.collection.JavaConversions._
import com.echoed.cache.CacheManager


class TwitterAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[TwitterAccessActor])

    @BeanProperty var consumerKey: String = _ //Called Consumer Key for Twitter
    @BeanProperty var consumerSecret: String = _  //Called Consumer Secret for Twitter
    @BeanProperty var callbackUrl: String = _  //Callback Url

    @BeanProperty var cacheManager: CacheManager = _

    @BeanProperty var properties: Properties = _

    import scala.collection.mutable.ConcurrentMap

    private var cache: ConcurrentMap[String, Twitter] = null


    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        // where placeholder values were not being resolved
        {
            cache = cacheManager.getCache[Twitter]("Twitters") //, Some(new CacheListenerActorClient(self)))
            consumerKey = properties.getProperty("consumerKey")
            consumerSecret = properties.getProperty("consumerSecret")
            //callbackUrl = properties.getProperty("callbackUrl")
            consumerKey != null && consumerSecret != null //&& callbackUrl != null
        } ensuring(_ == true, "Missing parameters")
    }

    def receive = {
        case msg @ FetchRequestToken(callbackUrl) =>
            val channel: Channel[FetchRequestTokenResponse] = self.channel

            try {
                val twitterHandler = buildTwitter()
                val requestToken = twitterHandler.getOAuthRequestToken(callbackUrl)
                cache(makeRequestTokenKey(requestToken)) = twitterHandler
                channel ! FetchRequestTokenResponse(msg, Right(requestToken))
            } catch {
                case e =>
                    channel ! FetchRequestTokenResponse(msg, Left(TwitterException("Cannot get request token", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ GetAccessTokenForRequestToken(requestToken, oAuthVerifier) =>
            val channel: Channel[GetAccessTokenForRequestTokenResponse] = self.channel

            try {
                val requestTokenKey = makeRequestTokenKey(requestToken)
                val twitterHandler = cache(requestTokenKey)
                val accessToken = twitterHandler.getOAuthAccessToken(requestToken, oAuthVerifier)
                cache(makeAccessTokenKey(accessToken.getToken)) = twitterHandler
                channel ! GetAccessTokenForRequestTokenResponse(msg, Right(accessToken))
                cache -= requestTokenKey //no need to hang on to this
            } catch {
                case e =>
                    channel ! GetAccessTokenForRequestTokenResponse(
                            msg,
                            Left(TwitterException("Cannot get access token", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ FetchAccessToken(accessToken, accessTokenSecret) =>
            val channel: Channel[FetchAccessTokenResponse] = self.channel

            try {
                val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
                channel ! FetchAccessTokenResponse(msg, Right(twitterHandler.getOAuthAccessToken()))
            } catch {
                case e =>
                    channel ! FetchAccessTokenResponse(msg, Left(TwitterException("Cannot get access token", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ FetchUser(accessToken, accessTokenSecret, userId) =>
            val channel: Channel[FetchUserResponse] = self.channel

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
                channel ! FetchUserResponse(msg, Right(twitterUser))
            } catch {
                case e =>
                    channel ! FetchUserResponse(msg, Left(TwitterException("Cannot get user", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ FetchFollowers(accessToken, accessTokenSecret, twitterUserId, twitterId) =>
            val channel: Channel[FetchFollowersResponse] = self.channel

            try {
                logger.debug("Attempting to get Twitter followers for {}", twitterId)
                val ids = getTwitterHandler(accessToken, accessTokenSecret).getFollowersIDs(twitterId, -1).getIDs
                logger.debug("Twitter user {} has {} followers", twitterUserId, ids.length)
                val twitterFollowers = ids.map( id => new TwitterFollower(twitterUserId, id.toString, null)).toList
                logger.debug("Successfully created {} TwitterFollwers", twitterFollowers.size)
                channel ! FetchFollowersResponse(msg, Right(twitterFollowers))
            } catch {
                case e =>
                    channel ! FetchFollowersResponse(msg, Left(TwitterException("Cannot get Twitter followers", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ UpdateStatus(accessToken, accessTokenSecret, twitterStatus) =>
            val channel: Channel[UpdateStatusResponse] = self.channel

            try {
                val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
                val status = twitterHandler.updateStatus(twitterStatus.message)
                channel ! UpdateStatusResponse(msg, Right(twitterStatus.copy(
                        twitterId = status.getId.toString,
                        createdAt = status.getCreatedAt,
                        text = status.getText,
                        source = status.getSource)))
            } catch {
                case e =>
                    channel ! UpdateStatusResponse(msg, Left(TwitterException("Cannot update Twitter status", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case msg @ Logout(accessToken) =>
            val channel: Channel[LogoutResponse] = self.channel

            try {
                removeTwitterHandler(accessToken).cata(
                    twitterHandler => {
                        twitterHandler.shutdown()
                        channel ! LogoutResponse(msg, Right(true))
                        logger.debug("Successfully shutdown Twitter handler for accessToken {}", accessToken)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        logger.debug("Did not find Twitter handler for accessToken {}", accessToken)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(TwitterException("Cannot end Twitter session", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }
    }


    private def makeRequestTokenKey(requestToken: RequestToken) = "requestToken:%s" format requestToken.getToken

    private def makeAccessTokenKey(accessToken: String) = "accessToken:%s" format accessToken

    private def getTwitterHandler(accessToken: String, accessTokenSecret: String): Twitter =
        getTwitterHandler(makeAccessTokenKey(accessToken), accessToken, accessTokenSecret)

    private def getTwitterHandler(key: String, accessToken: String, accessTokenSecret: String) = {
        cache.getOrElse(key, {
            val twitter = buildTwitter(Option(accessToken), Option(accessTokenSecret))
            cache(key) = twitter
            twitter
        })
    }

    private def removeTwitterHandler(accessToken: String): Option[Twitter] = cache.remove(makeAccessTokenKey(accessToken))

    private def buildTwitter(accessToken: Option[String] = None, accessTokenSecret: Option[String] = None) = {
        val  configurationBuilder: ConfigurationBuilder = new ConfigurationBuilder()
        configurationBuilder.setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken.orNull)
                .setOAuthAccessTokenSecret(accessTokenSecret.orNull)

        new TwitterFactory(configurationBuilder.build()).getInstance()
    }

}
