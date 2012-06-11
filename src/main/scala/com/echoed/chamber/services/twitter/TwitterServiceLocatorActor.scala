package com.echoed.chamber.services.twitter


import reflect.BeanProperty
import scalaz._
import Scalaz._
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import akka.actor._
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.{ConcurrentHashMap => JConcurrentHashMap}
import com.echoed.chamber.services.ActorClient
import scala.collection.JavaConversions._
import com.echoed.chamber.dao.{TwitterStatusDao, TwitterUserDao}
import java.util.Properties
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import akka.event.Logging
import akka.actor.SupervisorStrategy.Restart
import twitter4j.auth.RequestToken
import org.springframework.beans.factory.FactoryBean


class TwitterServiceLocatorActor extends FactoryBean[ActorRef] {


    @BeanProperty var cacheManager: CacheManager = _

    @BeanProperty var twitterAccess: TwitterAccess = _
    @BeanProperty var twitterUserDao: TwitterUserDao = _
    @BeanProperty var twitterStatusDao: TwitterStatusDao = _
    @BeanProperty var urlsProperties: Properties = _

    var echoClickUrl: String = _


    private var cache: ConcurrentMap[String, TwitterService] = new JConcurrentHashMap[String, TwitterService]()
    private var idCache: ConcurrentMap[String, TwitterService] = null


    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    override def preStart() {
        idCache = cacheManager.getCache[TwitterService]("TwitterServices", Some(new CacheListenerActorClient(self)))
        echoClickUrl = urlsProperties.getProperty("echoClickUrl")
        assert(echoClickUrl != null)
    }

    def receive = {
        case msg @ CacheEntryRemoved(twitterUserId: String, twitterService: TwitterService, cause: String) =>
            logger.debug("Received {}", msg)
            twitterService.logout(twitterUserId)
            for ((key, ts) <- cache if (ts.id == twitterService.id)) {
                cache.remove(key).foreach(_.asInstanceOf[ActorClient].actorRef ! PoisonPill)
                logger.debug("Removed {} from cache", ts.id)
            }
            logger.debug("Sent logout for {}", twitterService)

        case msg @ GetTwitterService(callbackUrl) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetTwitterServiceResponse(msg, Left(TwitterException("Cannot get Twitter service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating new TwitterService with callbackUrl {}", callbackUrl)
                (me ? CreateTwitterService(callbackUrl)).onSuccess {
                    case CreateTwitterServiceResponse(_, Left(e)) => error(e)
                    case CreateTwitterServiceResponse(_, Right(twitterService)) =>
                        channel ! GetTwitterServiceResponse(msg, Right(twitterService))

                        twitterService.getRequestToken.onSuccess {
                            case GetRequestTokenResponse(_, Left(e)) => throw e
                            case GetRequestTokenResponse(_, Right(requestToken)) =>
                                logger.debug("Caching TwitterService with token {}", requestToken.getToken)
                                cache.put("requestToken:" + requestToken.getToken, twitterService)
                        }.onFailure {
                            case e => logger.error("Unexpected error when trying to cache TwitterService with request token", e)
                        }
                }.onFailure { case e => error(e) }
            } catch { case e => error(e) }


        case msg @ GetTwitterServiceWithToken(oAuthToken) =>
            val channel = context.sender

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
            val me = context.self
            val channel = context.sender

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
                                    case e => logger.error("Unexpected error when trying to cache TwitterService %s" format msg, e)
                                }
                        }.onFailure { case e => error(e) }
                    })
            } catch { case e => error(e) }


        case msg @ GetTwitterServiceWithId(id) =>
            val me = context.self
            val channel = context.sender

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
                        (me ? CreateTwitterServiceWithId(id)).onSuccess {
                            case CreateTwitterServiceWithIdResponse(_, Left(e: TwitterUserNotFound)) =>
                                channel ! GetTwitterServiceWithIdResponse(msg, Left(e))
                            case CreateTwitterServiceWithIdResponse(_, Left(e)) => error(e)
                            case CreateTwitterServiceWithIdResponse(_, Right(twitterService)) =>
                                channel ! GetTwitterServiceWithIdResponse(msg, Right(twitterService))
                                idCache += (id -> twitterService)
                                logger.debug("Cached TwitterService with id {}", id)
                        }.onFailure { case e => error(e) }
                    })
            } catch { case e => error(e) }


        case msg @ Logout(twitterUserId) =>
            val channel = context.sender

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


        case ('requestToken, requestToken: RequestToken, msg: CreateTwitterService, channel: ActorRef) =>
            channel ! CreateTwitterServiceResponse(msg, Right(new TwitterServiceActorClient(context.actorOf(Props(
                new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, requestToken, echoClickUrl)),
                requestToken.getAuthorizationURL))))

        case msg @ CreateTwitterService(callbackUrl) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateTwitterServiceResponse(msg, Left(TwitterException("Unexpected error creating Twitter service", e)))
                logger.error("Error creating Twitter service %s" format msg, e)
            }

            try {
                logger.debug("Creating new TwitterService with callback {}", callbackUrl)
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
                logger.error("Error creating Twitter service %s" format msg, e)
            }

            try {
                logger.debug("Creating new Twitter service With access token {} for user {}", accessToken.getToken, accessToken.getUserId)
                twitterAccess.getUser(accessToken.getToken, accessToken.getTokenSecret, accessToken.getUserId).onComplete(_.fold(
                    e => error(e),
                    _ match {
                        case FetchUserResponse(_, Left(e)) => error(e)
                        case FetchUserResponse(_, Right(tu)) =>
                            logger.debug("Looking up twitter user with twitterId {}", accessToken.getUserId)
                            val twitterUser = Option(twitterUserDao.findByTwitterId(accessToken.getUserId.toString)).cata(
                                u => {
                                    logger.debug("Found TwitterUser {} with Twitter id {}", u, accessToken.getUserId)
                                    val t = u.copy(
                                        name = tu.name,
                                        profileImageUrl = tu.profileImageUrl,
                                        location = tu.location,
                                        timezone = tu.timezone,
                                        accessToken = accessToken.getToken,
                                        accessTokenSecret = accessToken.getTokenSecret)
                                    twitterUserDao.update(t)
                                    logger.debug("Successfully updated {}", t)
                                    t
                                },
                                {
                                    twitterUserDao.insert(tu)
                                    logger.debug("Successfully inserted {}", tu)
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
                logger.error("Error creating Twitter service %s" format msg, e)
            }

            try {
                logger.debug("Creating new TwitterService With id {}", id)
                Option(twitterUserDao.findById(id)).cata(
                    twitterUser => {
                        channel ! CreateTwitterServiceWithIdResponse(msg, Right(
                            new TwitterServiceActorClient(context.actorOf(Props().withCreator {
                                val tu = Option(twitterUserDao.findById(id)).get
                                new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, echoClickUrl, tu)
                            }, id))))
                        logger.debug("Created TwitterService with id {}", id)
                    },
                    {
                        channel ! CreateTwitterServiceWithIdResponse(
                                msg,
                                Left(TwitterUserNotFound(id)))
                        logger.debug("Twitter user with id {} not found", id)
                    })
            } catch { case e => error(e) }
    }

    }), "TwitterServiceManager")
}

