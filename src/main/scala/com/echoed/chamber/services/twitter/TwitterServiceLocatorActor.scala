package com.echoed.chamber.services.twitter

import scalaz._
import Scalaz._
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import akka.actor._
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.{ConcurrentHashMap => JConcurrentHashMap}
import com.echoed.chamber.services.ActorClient
import com.echoed.chamber.dao.{TwitterStatusDao, TwitterUserDao}
import akka.util.duration._
import akka.pattern.ask
import akka.actor.SupervisorStrategy.Restart
import twitter4j.auth.RequestToken
import akka.util.Timeout
import scala.collection.JavaConversions._


class TwitterServiceLocatorActor(
        cacheManager: CacheManager,
        twitterAccess: TwitterAccess,
        twitterUserDao: TwitterUserDao,
        twitterStatusDao: TwitterStatusDao,
        echoClickUrl: String,
        implicit val timeout: Timeout = Timeout(20000)) extends Actor with ActorLogging {

    private val cache: ConcurrentMap[String, TwitterService] = new JConcurrentHashMap[String, TwitterService]()
    private val idCache: ConcurrentMap[String, TwitterService] =
            cacheManager.getCache[TwitterService]("TwitterServices", Some(new CacheListenerActorClient(self)))

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    def receive = {
        case msg @ CacheEntryRemoved(twitterUserId: String, twitterService: TwitterService, cause: String) =>
            log.debug("Received {}", msg)
            twitterService.logout(twitterUserId)
            for ((key, ts) <- cache if (ts.id == twitterService.id)) {
                cache.remove(key).foreach(_.asInstanceOf[ActorClient].actorRef ! PoisonPill)
                log.debug("Removed {} from cache", ts.id)
            }
            log.debug("Sent logout for {}", twitterService)

        case msg @ GetTwitterService(callbackUrl) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetTwitterServiceResponse(msg, Left(TwitterException("Cannot get Twitter service", e)))
                log.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                log.debug("Creating new TwitterService with callbackUrl {}", callbackUrl)
                (me ? CreateTwitterService(callbackUrl)).onSuccess {
                    case CreateTwitterServiceResponse(_, Left(e)) => error(e)
                    case CreateTwitterServiceResponse(_, Right(twitterService)) =>
                        channel ! GetTwitterServiceResponse(msg, Right(twitterService))

                        twitterService.getRequestToken.onSuccess {
                            case GetRequestTokenResponse(_, Left(e)) => throw e
                            case GetRequestTokenResponse(_, Right(requestToken)) =>
                                log.debug("Caching TwitterService with token {}", requestToken.getToken)
                                cache.put("requestToken:" + requestToken.getToken, twitterService)
                        }.onFailure {
                            case e => log.error("Unexpected error when trying to cache TwitterService with request token: {}", e)
                        }
                }.onFailure { case e => error(e) }
            } catch { case e => error(e) }


        case msg @ GetTwitterServiceWithToken(oAuthToken) =>
            val channel = context.sender

            try {
                log.debug("Locating TwitterService with request token {}", oAuthToken)
                cache.get("requestToken:" + oAuthToken).cata(
                    twitterService => {
                        channel ! GetTwitterServiceWithTokenResponse(msg, Right(twitterService))
                        log.debug("Cache hit for TwitterService with request token {}", oAuthToken)
                    },
                    {
                        channel ! GetTwitterServiceWithTokenResponse(msg, Left(TwitterException("No Twitter service")))
                        log.debug("Cache miss for TwitterService with request token {}", oAuthToken)
                    })
            } catch {
                case e =>
                    channel ! GetTwitterServiceWithTokenResponse(msg, Left(TwitterException("Error getting Twitter service", e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
            }


        case msg @ GetTwitterServiceWithAccessToken(accessToken) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetTwitterServiceWithAccessTokenResponse(msg, Left(TwitterException("Cannot get Twitter service", e)))
                log.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                val accessTokenKey = "accessToken:" + accessToken.getToken
                log.debug("Looking in cache for accessTokenKey {}", accessTokenKey)

                cache.get(accessTokenKey).cata(
                    twitterService => {
                        channel ! GetTwitterServiceWithAccessTokenResponse(msg, Right(twitterService))
                        log.debug("Cache hit for TwitterService with cache key {}", accessTokenKey)
                    },
                    {
                        log.debug("Cache miss for TwitterService with cache key {}", accessTokenKey)
                        (me ? CreateTwitterServiceWithAccessToken(accessToken)).onSuccess {
                            case CreateTwitterServiceWithAccessTokenResponse(_, Left(e)) => error(e)
                            case CreateTwitterServiceWithAccessTokenResponse(_, Right(twitterService)) =>
                                channel ! GetTwitterServiceWithAccessTokenResponse(msg, Right(twitterService))
                                cache += (accessTokenKey -> twitterService)

                                twitterService.getUser.onSuccess {
                                    case GetUserResponse(_, Left(e)) => throw e
                                    case GetUserResponse(_, Right(twitterUser)) =>
                                        idCache += (twitterUser.id -> twitterService)
                                }.onFailure {
                                    case e => log.error("Unexpected error when trying to cache TwitterService {}: {}", msg, e)
                                }
                        }.onFailure { case e => error(e) }
                    })
            } catch { case e => error(e) }


        case msg @ GetTwitterServiceWithId(id) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetTwitterServiceWithIdResponse(msg, Left(TwitterException("Cannot get Twitter service", e)))
                log.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                idCache.get(id).cata(
                    twitterService => {
                        channel ! GetTwitterServiceWithIdResponse(msg, Right(twitterService))
                        log.debug("Cache hit for TwitterService with id {}", id)
                    },
                    {
                        log.debug("Cache miss for TwitterService with id {}", id)
                        (me ? CreateTwitterServiceWithId(id)).onSuccess {
                            case CreateTwitterServiceWithIdResponse(_, Left(e: TwitterUserNotFound)) =>
                                channel ! GetTwitterServiceWithIdResponse(msg, Left(e))
                            case CreateTwitterServiceWithIdResponse(_, Left(e)) => error(e)
                            case CreateTwitterServiceWithIdResponse(_, Right(twitterService)) =>
                                channel ! GetTwitterServiceWithIdResponse(msg, Right(twitterService))
                                idCache += (id -> twitterService)
                                log.debug("Cached TwitterService with id {}", id)
                        }.onFailure { case e => error(e) }
                    })
            } catch { case e => error(e) }


        case msg @ Logout(twitterUserId) =>
            val channel = context.sender

            try {
                log.debug("Processing {}", msg)
                idCache.remove(twitterUserId).cata(
                    ts => {
                        channel ! LogoutResponse(msg, Right(true))
                        log.debug("Logged out TwitterUser {} ", twitterUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        log.debug("Did not find TwitterUser to {}", msg)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(TwitterException("Could not logout Twitter user", e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
            }


        case ('requestToken, requestToken: RequestToken, msg: CreateTwitterService, channel: ActorRef) =>
            channel ! CreateTwitterServiceResponse(msg, Right(new TwitterServiceActorClient(context.actorOf(Props(
                new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, requestToken, echoClickUrl)),
                requestToken.getToken))))

        case msg @ CreateTwitterService(callbackUrl) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateTwitterServiceResponse(msg, Left(TwitterException("Unexpected error creating Twitter service", e)))
                log.error("Error creating Twitter service {}: {}", msg, e)
            }

            try {
                log.debug("Creating new TwitterService with callback {}", callbackUrl)
                twitterAccess.getRequestToken(callbackUrl).onComplete(_.fold(
                    e => error(e),
                    _ match {
                        case FetchRequestTokenResponse(_, Right(requestToken)) =>
                            me ! ('requestToken, requestToken, msg, channel)
                        case FetchRequestTokenResponse(_, Left(e)) => error(e)
                    }))
            } catch { case e => error(e) }


        case msg @ CreateTwitterServiceWithAccessToken(accessToken) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateTwitterServiceWithAccessTokenResponse(msg, Left(TwitterException("Unexpected error creating Twitter service", e)))
                log.error("Error creating Twitter service {}: {}", msg, e)
            }

            try {
                log.debug("Creating new Twitter service With access token {} for user {}", accessToken.getToken, accessToken.getUserId)
                twitterAccess.getUser(accessToken.getToken, accessToken.getTokenSecret, accessToken.getUserId).onComplete(_.fold(
                    e => error(e),
                    _ match {
                        case FetchUserResponse(_, Left(e)) => error(e)
                        case FetchUserResponse(_, Right(tu)) =>
                            log.debug("Looking up twitter user with twitterId {}", accessToken.getUserId)
                            val twitterUser = Option(twitterUserDao.findByTwitterId(accessToken.getUserId.toString)).cata(
                                u => {
                                    log.debug("Found TwitterUser {} with Twitter id {}", u, accessToken.getUserId)
                                    val t = u.copy(
                                        name = tu.name,
                                        profileImageUrl = tu.profileImageUrl,
                                        location = tu.location,
                                        timezone = tu.timezone,
                                        accessToken = accessToken.getToken,
                                        accessTokenSecret = accessToken.getTokenSecret)
                                    twitterUserDao.update(t)
                                    log.debug("Successfully updated {}", t)
                                    t
                                },
                                {
                                    twitterUserDao.insert(tu)
                                    log.debug("Successfully inserted {}", tu)
                                    tu
                                })

                            (me ? CreateTwitterServiceWithId(twitterUser.id)).onComplete(_.fold(
                                error(_),
                                _ match {
                                    case CreateTwitterServiceWithIdResponse(_, Left(e)) => error(e)
                                    case CreateTwitterServiceWithIdResponse(_, Right(ts)) =>
                                        channel ! CreateTwitterServiceWithAccessTokenResponse(msg, Right(ts))
                                }))
                    }))
            } catch { case e => error(e) }


        case msg @ CreateTwitterServiceWithId(id) =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateTwitterServiceWithIdResponse(msg, Left(TwitterException("Unexpected error creating Twitter service", e)))
                log.error("Error creating Twitter service {}: {}", msg, e)
            }

            try {
                idCache.get(id).cata(
                    ts => channel ! CreateTwitterServiceWithIdResponse(msg, Right(ts)),
                    {
                        log.debug("Creating new TwitterService With id {}", id)
                        Option(twitterUserDao.findById(id)).cata(
                            twitterUser => {
                                channel ! CreateTwitterServiceWithIdResponse(msg, Right(
                                    new TwitterServiceActorClient(context.actorOf(Props().withCreator {
                                        val tu = Option(twitterUserDao.findById(id)).get
                                        new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, echoClickUrl, tu)
                                    }, id))))
                                log.debug("Created TwitterService with id {}", id)
                            },
                            {
                                channel ! CreateTwitterServiceWithIdResponse(
                                        msg,
                                        Left(TwitterUserNotFound(id)))
                                log.debug("Twitter user with id {} not found", id)
                            })
                    })
            } catch { case e => error(e) }
    }

}

