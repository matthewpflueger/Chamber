package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import scalaz._
import Scalaz._
import com.echoed.cache.{CacheEntryRemoved, CacheManager, CacheListenerActorClient}
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import java.util.Properties
import com.echoed.chamber.dao.partner.{PartnerSettingsDao, PartnerDao}
import com.echoed.chamber.dao.{FacebookUserDao, FacebookPostDao, FacebookFriendDao}
import org.springframework.beans.factory.FactoryBean
import akka.util.duration._
import akka.util.Timeout
import akka.event.Logging
import akka.pattern.ask
import com.echoed.chamber.domain.FacebookUser
import akka.actor._
import akka.actor.SupervisorStrategy.Restart


class FacebookServiceLocatorActor extends FactoryBean[ActorRef] {

    @BeanProperty var cacheManager: CacheManager = _

    @BeanProperty var facebookAccess: FacebookAccess = _
    @BeanProperty var facebookUserDao: FacebookUserDao = _
    @BeanProperty var facebookPostDao: FacebookPostDao = _
    @BeanProperty var facebookFriendDao: FacebookFriendDao = _
    @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _
    @BeanProperty var partnerDao: PartnerDao = _
    @BeanProperty var urlsProperties: Properties = _

    var echoClickUrl: String = _

    private var cache: ConcurrentMap[String, FacebookService] = null
    private val cacheByFacebookId: ConcurrentMap[String, FacebookService] = new ConcurrentHashMap[String, FacebookService]()


    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    private def updateMe(me: FacebookUser) = {
        val facebookUser = Option(facebookUserDao.findByFacebookId(me.facebookId)) match {
            case Some(fu) =>
                logger.debug("Found Facebook User {}", me.facebookId)
                fu.copy(accessToken = me.accessToken,
                        name = me.name,
                        email = me.email,
                        facebookId = me.facebookId,
                        link = me.link,
                        gender = me.gender,
                        timezone = me.timezone,
                        locale = me.locale)
            case None =>
                logger.debug("No Facebook User {}", me.facebookId)
                me
        }

        logger.debug("Updating FacebookUser {} accessToken {}", facebookUser.id, facebookUser.accessToken)
        facebookUserDao.insertOrUpdate(facebookUser)
        facebookUser
    }

    override def preStart() {
        cache = cacheManager.getCache[FacebookService]("FacebookServices", Some(new CacheListenerActorClient(self)))
        echoClickUrl = urlsProperties.getProperty("echoClickUrl")
        assert(echoClickUrl != null)
    }

    def receive = {
        case msg @ CacheEntryRemoved(facebookUserId: String, facebookService: FacebookService, cause: String) =>
            logger.debug("Received {}", msg)
            facebookService.logout(facebookUserId)
            for ((key, fs) <- cacheByFacebookId if (fs.id == facebookService.id)) {
                cacheByFacebookId -= key
                logger.debug("Removed {} from cache by Facebook id", fs.id)
            }
            logger.debug("Sent logout for {}", facebookService)


        case msg @ LocateByCode(code, queryString) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! LocateByCodeResponse(msg, Left(FacebookException("Could not locate Facebook user", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Locating FacebookService with code {}", code)
                (me ? CreateFromCode(code, queryString)).onComplete(_.fold(
                    error(_),
                    _ match {
                        case CreateFromCodeResponse(_, Left(e)) => error(e)
                        case CreateFromCodeResponse(_, Right(facebookService)) =>
                            channel ! LocateByCodeResponse(msg, Right(facebookService))
                            facebookService.getFacebookUser.onComplete(_.fold(
                                logger.error("Failed to cache FacebookService for code %s" format msg, _),
                                _ match {
                                    case GetFacebookUserResponse(_, Left(e)) =>
                                        logger.error("Failed to cache FacebookService for code %s" format msg, e)
                                    case GetFacebookUserResponse(_, Right(facebookUser)) =>
                                        cache.put(facebookUser.id, facebookService)
                                        logger.debug("Seeded cache with FacebookService key {}", code)
                                }))
                    }))
            } catch { case e => error(e) }


        case msg @ LocateByFacebookId(facebookId, accessToken) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! LocateByFacebookIdResponse(msg, Left(FacebookException("Could not locate Facebook service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            def updateCache(facebookService: FacebookService) {
                facebookService.getFacebookUser.onComplete(_.fold(
                    logger.error("Unable to update FacebookService cache by id", _),
                    _ match {
                        case GetFacebookUserResponse(_, Left(e)) =>
                            logger.error("Unable to update FacebookService cache by id", e)
                        case GetFacebookUserResponse(_, Right(facebookUser)) =>
                            cache.put(facebookUser.id, facebookService)
                            logger.debug("Updated FacebookService cache by id for {}", facebookUser.id)
                    }))
            }

            try {
                cacheByFacebookId.get(facebookId) match {
                    case Some(facebookService) =>
                        logger.debug("Cache hit for FacebookService with facebookId {}", facebookId)
                        channel ! LocateByFacebookIdResponse(msg, Right(facebookService))
                        facebookService.updateAccessToken(accessToken)
                        updateCache(facebookService)
                    case None =>
                        logger.debug("Cache miss for FacebookService with facebookId {}", facebookId)
                        (me ? CreateFromFacebookId(facebookId, accessToken)).onComplete(_.fold(
                            error(_),
                            _ match {
                                case CreateFromFacebookIdResponse(_, Left(e)) => error(e)
                                case CreateFromFacebookIdResponse(_, Right(facebookService)) =>
                                    channel ! LocateByFacebookIdResponse(msg, Right(facebookService))
                                    cacheByFacebookId.put(facebookId, facebookService)
                                    updateCache(facebookService)
                            }))
                }
            } catch { case e => error(e) }


        case msg @ LocateById(facebookUserId) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! LocateByIdResponse(msg, Left(FacebookException("Could not locate Facebook user", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                cache.get(facebookUserId) match {
                    case Some(facebookService) =>
                        logger.debug("Cache hit for FacebookService with facebookUserId {}", facebookUserId)
                        channel ! LocateByIdResponse(msg, Right(facebookService))
                    case None =>
                        logger.debug("Cache miss for FacebookService with facebookUserId {}", facebookUserId)
                        (me ? CreateFromId(facebookUserId)).onComplete(_.fold(
                            error(_),
                            _ match {
                                case CreateFromIdResponse(_, Left(e: FacebookUserNotFound)) =>
                                    channel ! LocateByIdResponse(msg, Left(e))
                                    logger.debug("Facebook user {} not found", facebookUserId)
                                case CreateFromIdResponse(_, Left(e)) => error(e)
                                case CreateFromIdResponse(_, Right(facebookService)) =>
                                    channel ! LocateByIdResponse(msg, Right(facebookService))
                                    cache.put(facebookUserId, facebookService)
                                    logger.debug("Cached {}", facebookService)
                            }))
                }
            } catch { case e => error(e) }


        case msg @ Logout(facebookUserId) =>
            val channel = context.sender

            try {
                logger.debug("Processing {}", msg)
                cache.remove(facebookUserId).cata(
                    fs => {
                        channel ! LogoutResponse(msg, Right(true))
                        logger.debug("Logged out FacebookUser {} ", facebookUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        logger.debug("Did not find FacebookUser to {}", msg)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(FacebookException("Could not logout Facebook user", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }


        case msg @ CreateFromCode(code, queryString) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateFromCodeResponse(msg, Left(FacebookException("Could not create Facebook service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating FacebookService using code {}", code)
                facebookAccess.getMe(code, queryString).onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetMeResponse(_, Left(e)) => error(e)
                        case GetMeResponse(_, Right(fu)) =>
                            val facebookUser = updateMe(fu)
                            (me ? CreateFromId(facebookUser.id)).onComplete(_.fold(
                                error(_),
                                _ match {
                                    case CreateFromIdResponse(_, Left(e)) => error(e)
                                    case CreateFromIdResponse(_, Right(facebookService)) =>
                                        channel ! CreateFromCodeResponse(msg, Right(facebookService))
                                        logger.debug("Created FacebookService with user {}", facebookUser)
                                }))
                    }))
            } catch { case e => error(e) }


        case msg @ CreateFromId(facebookUserId) =>
            val channel = context.sender

            try {
                logger.debug("Creating FacebookService using facebookUserId {}", facebookUserId)
                Option(facebookUserDao.findById(facebookUserId)) match {
                    case Some(facebookUser) =>
                        channel ! CreateFromIdResponse(msg, Right(
                            new FacebookServiceActorClient(context.actorOf(Props().withCreator {
                                val fu = Option(facebookUserDao.findById(facebookUserId)).get
                                new FacebookServiceActor(
                                    fu,
                                    facebookAccess,
                                    facebookUserDao,
                                    facebookPostDao,
                                    facebookFriendDao,
                                    echoClickUrl)
                            }, facebookUserId))))
                        logger.debug("Created Facebook service {}", facebookUserId)
                    case None =>
                        channel ! CreateFromIdResponse(msg, Left(FacebookUserNotFound(facebookUserId)))
                        logger.debug("Did not find FacebookUser with id {}", facebookUserId)
                }
            } catch {
                case e =>
                    channel ! CreateFromIdResponse(msg, Left(FacebookException("Could not create Facebook service", e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ CreateFromFacebookId(facebookId, accessToken) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateFromFacebookIdResponse(msg, Left(FacebookException("Could not create Facebook service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating FacebookService using facebookId {}", facebookId)
                facebookAccess.fetchMe(accessToken).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchMeResponse(_, Left(e)) => error(e)
                        case FetchMeResponse(_, Right(fu)) =>
                            val facebookUser = updateMe(fu)
                            (me ? CreateFromId(facebookUser.id)).onComplete(_.fold(
                                error(_),
                                _ match {
                                    case CreateFromIdResponse(_, Left(e)) => error(e)
                                    case CreateFromIdResponse(_, Right(facebookService)) =>
                                        channel ! CreateFromFacebookIdResponse(msg, Right(facebookService))
                                        logger.debug("Created FacebookService from Facebook id {}", facebookId)
                                }))
                    }))
            } catch { case e => error(e) }
    }

    }), "FacebookServiceManager")

}
