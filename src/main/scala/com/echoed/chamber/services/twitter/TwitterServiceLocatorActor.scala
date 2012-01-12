package com.echoed.chamber.services.twitter


import collection.mutable.WeakHashMap
import reflect.BeanProperty
import akka.util.Duration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import twitter4j.auth.{RequestToken, AccessToken}
import scalaz._
import Scalaz._
import akka.actor.{Channel, Actor}


class TwitterServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[TwitterServiceLocatorActor])

    @BeanProperty var twitterServiceCreator: TwitterServiceCreator = _

    private val cache = WeakHashMap[String, TwitterService]()
    private val idCache = WeakHashMap[String, TwitterService]() //twitterId -> TwitterService

    def receive = {

        case msg @ GetTwitterService(callbackUrl) =>
            val channel: Channel[GetTwitterServiceResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetTwitterServiceResponse(msg, Left(TwitterException("Cannot get Twitter service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating new TwitterService with callbackUrl {}", callbackUrl)
                twitterServiceCreator.createTwitterService(callbackUrl).onResult {
                    case CreateTwitterServiceResponse(_, Left(e)) => error(e)
                    case CreateTwitterServiceResponse(_, Right(twitterService)) =>
                        channel ! GetTwitterServiceResponse(msg, Right(twitterService))

                        twitterService.getRequestToken.onResult {
                            case GetRequestTokenResponse(_, Left(e)) => throw e
                            case GetRequestTokenResponse(_, Right(requestToken)) =>
                                logger.debug("Caching TwitterService with token {}", requestToken.getToken)
                                cache += ("requestToken:" + requestToken.getToken -> twitterService)
                        }.onException {
                            case e => logger.error("Unexpected error when trying to cache TwitterService with request token", e)
                        }
                }.onException { case e => error(e) }
            } catch { case e => error(e) }


        case msg @ GetTwitterServiceWithToken(oAuthToken) =>
            val channel: Channel[GetTwitterServiceWithTokenResponse] = self.channel

            try {
                logger.debug("Locating TwitterService with request token {}", oAuthToken)
                cache.get("requestToken:" + oAuthToken).cata(
                    twitterService => {
                        channel ! GetTwitterServiceWithTokenResponse(msg, Right(twitterService))
                        logger.debug("Cache hit for TwitterService with request token {}", oAuthToken)
                    },
                    {
                        channel ! GetTwitterServiceWithTokenResponse(msg, Left(TwitterException("No Twitter service")))
                        logger.debug("Cache miss for TwitterService with request token {}", oAuthToken)
                    })
            } catch {
                case e =>
                    channel ! GetTwitterServiceWithTokenResponse(msg, Left(TwitterException("Error getting Twitter service", e)))
                    logger.error("Unexpected error processing %s", msg, e)
            }


        case msg @ GetTwitterServiceWithAccessToken(accessToken) =>
            val channel: Channel[GetTwitterServiceWithAccessTokenResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetTwitterServiceWithAccessTokenResponse(msg, Left(TwitterException("Cannot get Twitter service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                val accessTokenKey = "accessToken:" + accessToken.getToken
                logger.debug("Looking in cache for accessTokenKey {}", accessTokenKey)

                cache.get(accessTokenKey).cata(
                    twitterService => {
                        channel ! GetTwitterServiceWithAccessTokenResponse(msg, Right(twitterService))
                        logger.debug("Cache hit for TwitterService with cache key {}", accessTokenKey)
                    },
                    {
                        logger.debug("Cache miss for TwitterService with cache key {}", accessTokenKey)
                        twitterServiceCreator.createTwitterServiceWithAccessToken(accessToken).onResult {
                            case CreateTwitterServiceWithAccessTokenResponse(_, Left(e)) => error(e)
                            case CreateTwitterServiceWithAccessTokenResponse(_, Right(twitterService)) =>
                                channel ! GetTwitterServiceWithAccessTokenResponse(msg, Right(twitterService))
                                cache += (accessTokenKey -> twitterService)

                                twitterService.getUser.onResult {
                                    case GetUserResponse(_, Left(e)) => throw e
                                    case GetUserResponse(_, Right(twitterUser)) =>
                                        idCache += (twitterUser.id -> twitterService)
                                }.onException {
                                    case e => logger.error("Unexpected error when trying to cache TwitterService %s" format msg, e)
                                }
                        }.onException { case e => error(e) }
                    })
            } catch { case e => error(e) }


        case msg @ GetTwitterServiceWithId(id) =>
            val channel: Channel[GetTwitterServiceWithIdResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetTwitterServiceWithIdResponse(msg, Left(TwitterException("Cannot get Twitter service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                idCache.get(id).cata(
                    twitterService => {
                        channel ! GetTwitterServiceWithIdResponse(msg, Right(twitterService))
                        logger.debug("Cache hit for TwitterService with id {}", id)
                    },
                    {
                        logger.debug("Cache miss for TwitterService with id {}", id)
                        twitterServiceCreator.createTwitterServiceWithId(id).onResult {
                            case CreateTwitterServiceWithIdResponse(_, Left(e: TwitterUserNotFound)) =>
                                channel ! GetTwitterServiceWithIdResponse(msg, Left(e))
                            case CreateTwitterServiceWithIdResponse(_, Left(e)) => error(e)
                            case CreateTwitterServiceWithIdResponse(_, Right(twitterService)) =>
                                channel ! GetTwitterServiceWithIdResponse(msg, Right(twitterService))
                                idCache += (id -> twitterService)
                                logger.debug("Cached TwitterService with id {}", id)
                        }.onException { case e => error(e) }
                    })
            } catch { case e => error(e) }
    }
}

