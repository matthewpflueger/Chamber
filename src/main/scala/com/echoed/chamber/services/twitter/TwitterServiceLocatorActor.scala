package com.echoed.chamber.services.twitter


import akka.actor.Actor
import collection.mutable.WeakHashMap
import reflect.BeanProperty
import akka.util.Duration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import twitter4j.auth.{RequestToken, AccessToken}

class TwitterServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[TwitterServiceLocatorActor])

    @BeanProperty var twitterServiceCreator: TwitterServiceCreator = _

    private val cache = WeakHashMap[String, TwitterService]()
    private val idCache = WeakHashMap[String, TwitterService]() //hashmap of TwitterUserId : TwitterService

    def receive = {
        case ("none") => {
            logger.debug("Creating New Twitter Service with No Token")
            val t = twitterServiceCreator.createTwitterService().await(Duration(10, TimeUnit.SECONDS)).get
            val requestToken: RequestToken = t.getRequestToken().get
            logger.debug("Caching Twitter Service {} with Token {}", t, requestToken.getToken)
            cache += ("requestToken:" + requestToken.getToken -> t)
            self.channel ! t
        }

        case ("requestToken", oAuthToken: String) => {
            logger.debug("Locating Twitter Service with Request Token {}", oAuthToken)
            val twitterService = cache.getOrElse("requestToken:" + oAuthToken, null)
            logger.debug("Received Twitter Service {} ", twitterService)
            self.channel ! twitterService
        }

        case ("accessToken", accessToken: AccessToken) => {
            val accessTokenKey: String = "accessToken:" + accessToken.getToken
            val channel = self.channel
            logger.debug("Looking in cache for accessTokenKey {}", accessTokenKey)
            val t = cache.getOrElse(accessTokenKey, {
                logger.debug("Creating new service with accessTokenKey {}", accessTokenKey)
                val twitterService = twitterServiceCreator.createTwitterServiceWithAccessToken(accessToken).await(Duration(10, TimeUnit.SECONDS)).get
                twitterService.getUser().map{
                    twitterUser =>
                        val twitterUserId = twitterUser.id
                        idCache += (twitterUserId -> twitterService)
                        cache += (accessTokenKey -> twitterService)
                }
                twitterService
            })
            channel ! t
        }

        case ("id", twitterUserId: String) => {
            val t = idCache.getOrElse(twitterUserId, {
                val twitterService = twitterServiceCreator.createTwitterServiceWithId(twitterUserId).await(Duration(10, TimeUnit.SECONDS)).get
                idCache += (twitterUserId -> twitterService)
                twitterService
            })
            self.channel ! t
        }
    }
}

