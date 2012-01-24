package com.echoed.chamber.services.twitter


import reflect.BeanProperty
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import akka.actor._
import scala.collection.mutable.{ConcurrentMap, WeakHashMap}


class TwitterServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[TwitterServiceLocatorActor])

    @BeanProperty var twitterServiceCreator: TwitterServiceCreator = _
    @BeanProperty var cacheManager: CacheManager = _

    private val cache = WeakHashMap[String, TwitterService]()
    private var idCache: ConcurrentMap[String, TwitterService] = null


    override def preStart() {
        idCache = cacheManager.getCache[TwitterService]("TwitterServices", Some(new CacheListenerActorClient(self)))
    }

    def receive = {
        case msg @ CacheEntryRemoved(twitterUserId: String, twitterService: TwitterService, cause: String) =>
            logger.debug("Received {}", msg)
            twitterService.logout(twitterUserId)
            for ((key, ts) <- cache if (ts.id == twitterService.id)) {
                cache -= key
                logger.debug("Removed {} from cache", ts.id)
            }
            logger.debug("Sent logout for {}", twitterService)

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


        case msg @ Logout(twitterUserId) =>
            val channel: Channel[LogoutResponse] = self.channel

            try {
                logger.debug("Processing {}", msg)
                idCache.remove(twitterUserId).cata(
                    ts => {
                        channel ! LogoutResponse(msg, Right(true))
                        logger.debug("Logged out TwitterUser {} ", twitterUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        logger.debug("Did not find TwitterUser to {}", msg)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(TwitterException("Could not logout Twitter user", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }
    }
}

