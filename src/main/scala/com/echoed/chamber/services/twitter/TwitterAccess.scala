package com.echoed.chamber.services.twitter

import com.echoed.chamber.domain.{Identifiable, TwitterFollower, TwitterUser}
import twitter4j.{TwitterFactory, Twitter}
import twitter4j.conf.ConfigurationBuilder
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import akka.actor._
import scala.collection.mutable.ConcurrentMap
import com.echoed.chamber.services.EchoedService
import com.echoed.chamber.services.echoeduser.{GetTwitterAuthenticationUrlResponse, GetTwitterAuthenticationUrl}


class TwitterAccess(
        consumerKey: String,
        consumerSecret: String,
        cacheManager: CacheManager) extends EchoedService {

    private var cache: ConcurrentMap[String, Twitter] =
            cacheManager.getCache[Twitter]("Twitters", Some(new CacheListenerActorClient(self)))

    private def getTwitterUser(accessToken: String, accessTokenSecret: String, userId: Long) = {
        val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
        val user = twitterHandler.showUser(userId)
        new TwitterUser(
                twitterId = user.getId.toString,
                screenName = user.getScreenName,
                name = user.getName,
                profileImageUrl = user.getProfileImageURL.toExternalForm,
                location = user.getLocation,
                timezone = user.getTimeZone,
                accessToken = accessToken,
                accessTokenSecret = accessTokenSecret)
    }

    def handle = {
        case msg @ CacheEntryRemoved(_, twitterHandler: Twitter, _) => twitterHandler.shutdown

        case msg @ GetTwitterAuthenticationUrl(callbackUrl) =>
            val twitterHandler = buildTwitter()
            val requestToken = twitterHandler.getOAuthRequestToken(callbackUrl)
            cache(requestToken.getToken) = twitterHandler
            context.sender ! GetTwitterAuthenticationUrlResponse(msg, Right(requestToken.getAuthenticationURL))


        case msg @ FetchUserForAuthToken(authToken, authVerifier) =>
            val twitterHandler = cache(authToken)
            val accessToken = twitterHandler.getOAuthAccessToken(authVerifier)
//            val accessToken = twitterHandler.getOAuthAccessToken(authToken, authVerifier)

            cache(accessToken.getToken) = twitterHandler
            context.sender ! FetchUserForAuthTokenResponse(
                    msg,
                    Right(getTwitterUser(accessToken.getToken, accessToken.getTokenSecret, accessToken.getUserId)))


        case msg @ FetchUser(accessToken, accessTokenSecret, userId) =>
            context.sender ! FetchUserResponse(msg, Right(getTwitterUser(accessToken, accessTokenSecret, userId)))


        case msg @ FetchFollowers(accessToken, accessTokenSecret, twitterUserId, twitterId) =>
            log.debug("Attempting to get Twitter followers for {}", twitterId)
            val ids = getTwitterHandler(accessToken, accessTokenSecret).getFollowersIDs(twitterId, -1).getIDs
            log.debug("Twitter user {} has {} followers", twitterUserId, ids.length)
            val twitterFollowers = ids.map( id => new TwitterFollower(twitterUserId, id.toString, null)).toList
            log.debug("Successfully created {} TwitterFollwers", twitterFollowers.size)
            context.sender ! FetchFollowersResponse(msg, Right(twitterFollowers))


        case msg @ UpdateStatus(accessToken, accessTokenSecret, twitterStatus) =>
            val twitterHandler = getTwitterHandler(accessToken, accessTokenSecret)
            val status = twitterHandler.updateStatus(twitterStatus.message)
            context.sender ! UpdateStatusResponse(msg, Right(twitterStatus.copy(
                    twitterId = status.getId.toString,
                    createdAt = status.getCreatedAt,
                    text = status.getText,
                    source = status.getSource)))
    }

    private def getTwitterHandler(accessToken: String, accessTokenSecret: String) =
        cache.getOrElseUpdate(accessToken, { buildTwitter(Option(accessToken), Option(accessTokenSecret)) })

    private def buildTwitter(accessToken: Option[String] = None, accessTokenSecret: Option[String] = None) = {
        val  configurationBuilder: ConfigurationBuilder = new ConfigurationBuilder()
        configurationBuilder.setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken.orNull)
                .setOAuthAccessTokenSecret(accessTokenSecret.orNull)

        new TwitterFactory(configurationBuilder.build()).getInstance()
    }

}
