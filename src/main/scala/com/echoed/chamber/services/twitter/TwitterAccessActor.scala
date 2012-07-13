package com.echoed.chamber.services.twitter

import com.echoed.chamber.domain.{TwitterFollower, TwitterUser}
import twitter4j.{TwitterFactory, Twitter}
import twitter4j.conf.ConfigurationBuilder
import twitter4j.User
import scalaz._
import Scalaz._
import twitter4j.auth.RequestToken
import com.echoed.cache.CacheManager
import akka.actor._
import scala.collection.mutable.ConcurrentMap
import com.echoed.chamber.services.EchoedActor


class TwitterAccessActor(
        consumerKey: String,
        consumerSecret: String,
        callbackUrl: String,
        cacheManager: CacheManager) extends EchoedActor {

    private var cache: ConcurrentMap[String, Twitter] = cacheManager.getCache[Twitter]("Twitters") //, Some(new CacheListenerActorClient(self))

    def handle = {
        case msg @ FetchRequestToken(callbackUrl) =>
            val channel = context.sender

            try {
                val twitterHandler = buildTwitter()
                val requestToken = twitterHandler.getOAuthRequestToken(callbackUrl)
                cache(makeRequestTokenKey(requestToken)) = twitterHandler
                channel ! FetchRequestTokenResponse(msg, Right(requestToken))
            } catch {
                case e =>
                    channel ! FetchRequestTokenResponse(msg, Left(TwitterException("Cannot get request token", e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
            }


        case msg @ GetAccessTokenForRequestToken(requestToken, oAuthVerifier) =>
            val channel = context.sender

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
                    log.error("Unexpected error processing {}: {}", msg, e)
            }


        case msg @ FetchAccessToken(accessToken, accessTokenSecret) =>
            val channel = context.sender

            try {
                val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
                channel ! FetchAccessTokenResponse(msg, Right(twitterHandler.getOAuthAccessToken()))
            } catch {
                case e =>
                    channel ! FetchAccessTokenResponse(msg, Left(TwitterException("Cannot get access token", e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
            }


        case msg @ FetchUser(accessToken, accessTokenSecret, userId) =>
            val channel = context.sender

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
                    log.error("Unexpected error processing {}: {}", msg, e)
            }


        case msg @ FetchFollowers(accessToken, accessTokenSecret, twitterUserId, twitterId) =>
            val channel = context.sender

            try {
                log.debug("Attempting to get Twitter followers for {}", twitterId)
                val ids = getTwitterHandler(accessToken, accessTokenSecret).getFollowersIDs(twitterId, -1).getIDs
                log.debug("Twitter user {} has {} followers", twitterUserId, ids.length)
                val twitterFollowers = ids.map( id => new TwitterFollower(twitterUserId, id.toString, null)).toList
                log.debug("Successfully created {} TwitterFollwers", twitterFollowers.size)
                channel ! FetchFollowersResponse(msg, Right(twitterFollowers))
            } catch {
                case e =>
                    channel ! FetchFollowersResponse(msg, Left(TwitterException("Cannot get Twitter followers", e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
            }


        case msg @ UpdateStatus(accessToken, accessTokenSecret, twitterStatus) =>
            val channel = context.sender

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
                    log.error("Unexpected error processing {}: {}", msg, e)
            }

        case msg @ Logout(accessToken) =>
            val channel = context.sender

            try {
                removeTwitterHandler(accessToken).cata(
                    twitterHandler => {
                        twitterHandler.shutdown()
                        channel ! LogoutResponse(msg, Right(true))
                        log.debug("Successfully shutdown Twitter handler for accessToken {}", accessToken)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        log.debug("Did not find Twitter handler for accessToken {}", accessToken)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(TwitterException("Cannot end Twitter session", e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
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
