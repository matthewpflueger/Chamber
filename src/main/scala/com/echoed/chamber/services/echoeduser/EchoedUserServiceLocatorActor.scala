package com.echoed.chamber.services.echoeduser

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import akka.actor._
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import scala.collection.mutable.ConcurrentMap
import org.springframework.beans.factory.FactoryBean
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import akka.event.Logging
import com.echoed.chamber.dao.views.{FeedDao, ClosetDao}
import com.echoed.chamber.dao.partner.PartnerSettingsDao
import com.echoed.chamber.dao.{EchoMetricsDao, EchoedFriendDao, EchoDao, EchoedUserDao}
import com.echoed.chamber.services.facebook.{FacebookServiceLocator, GetFacebookUserResponse}
import com.echoed.chamber.services.twitter.{TwitterServiceLocator, GetUserResponse}
import com.echoed.chamber.domain.EchoedUser
import akka.actor.SupervisorStrategy.Restart

class EchoedUserServiceLocatorActor extends FactoryBean[ActorRef] {


    @BeanProperty var echoedUserDao: EchoedUserDao = _
    @BeanProperty var closetDao: ClosetDao = _
    @BeanProperty var feedDao: FeedDao = _
    @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoedFriendDao: EchoedFriendDao = _
    @BeanProperty var echoMetricsDao: EchoMetricsDao = _

    @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _
    @BeanProperty var twitterServiceLocator: TwitterServiceLocator = _

    @BeanProperty var cacheManager: CacheManager = _


    private var cache: ConcurrentMap[String, EchoedUserService] = null


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
        cache = cacheManager.getCache[EchoedUserService]("EchoedUserServices", Some(new CacheListenerActorClient(self)))
    }

    def receive = {

        case msg @ CacheEntryRemoved(echoedUserId: String, echoedUserService: EchoedUserService, cause: String) =>
            logger.debug("Received {}", msg)
            echoedUserService.logout(echoedUserId)
            logger.debug("Sent logout for {}", echoedUserService)


        case msg @ LocateWithId(echoedUserId) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! LocateWithIdResponse(msg, Left(EchoedUserException("Could not locate Echoed user service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Locating EchoedUserService With id {}" , msg.echoedUserId)
                cache.get(echoedUserId) match {
                    case Some(echoedUserService) =>
                        channel ! LocateWithIdResponse(msg, Right(echoedUserService))
                        logger.debug("Cache hit for EchoedUserService key {}", echoedUserId)
                    case _ =>
                        logger.debug("Cache miss for EchoedUserService key {}", echoedUserId)
                        (me ? CreateEchoedUserServiceWithId(echoedUserId)).onComplete(_.fold(
                            e => error(e),
                            _ match {
                                case CreateEchoedUserServiceWithIdResponse(_, Left(e @ EchoedUserNotFound(id, _))) =>
                                    logger.debug("EchoedUser {} not found", id)
                                    channel ! LocateWithIdResponse(msg, Left(e))
                                case CreateEchoedUserServiceWithIdResponse(_, Left(e)) => error(e)
                                case CreateEchoedUserServiceWithIdResponse(_, Right(echoedUserService)) =>
                                    channel ! LocateWithIdResponse(msg, Right(echoedUserService))
                            }
                        ))
                }
            } catch { case e => error(e) }


        case msg @ LocateWithFacebookService(facebookService) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                LocateWithFacebookServiceResponse(msg, Left(EchoedUserException("Could not locate Echoed user service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                facebookService.getFacebookUser.onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetFacebookUserResponse(_, Left(e)) => error(e)
                        case GetFacebookUserResponse(_, Right(fu)) => Option(fu.echoedUserId).flatMap(cache.get(_)).cata(
                            echoedUserService => {
                                channel ! LocateWithFacebookServiceResponse(msg, Right(echoedUserService))
                                logger.debug("Cache hit for EchoedUserService for {}", facebookService)
                            },
                            {
                                logger.debug("Cache miss for EchoedUserService for {}", facebookService)
                                (me ? CreateEchoedUserServiceWithFacebookService(facebookService)).onComplete(_.fold(
                                    error(_),
                                    _ match {
                                        case CreateEchoedUserServiceWithFacebookServiceResponse(_, Left(e)) => error(e)
                                        case CreateEchoedUserServiceWithFacebookServiceResponse(_, Right(echoedUserService))=>
                                            channel ! LocateWithFacebookServiceResponse(msg, Right(echoedUserService))
                                    }))
                            })
                    }))
            } catch { case e => error(e) }


        case msg @ LocateWithTwitterService(twitterService) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! LocateWithTwitterServiceResponse(msg, Left(EchoedUserException("Could not locate Echoed user service", e)))
                logger.error("Error processing %s", msg, e)
            }

            try {
                twitterService.getUser.onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetUserResponse(_, Left(e)) => error(e)
                        case GetUserResponse(_, Right(tu)) => Option(tu.echoedUserId).flatMap(cache.get(_)).cata(
                            echoedUserService => {
                                channel ! LocateWithTwitterServiceResponse(msg, Right(echoedUserService))
                                logger.debug("Cache hit for EchoedUserService {} for {} ", tu.echoedUserId, twitterService)
                            },
                            {
                                logger.debug("Cache miss for EchoedUserService for {}", twitterService)
                                (me ? CreateEchoedUserServiceWithTwitterService(twitterService)).onComplete(_.fold(
                                    error(_),
                                    _ match {
                                        case CreateEchoedUserServiceWithTwitterServiceResponse(_, Left(e)) => error(e)
                                        case CreateEchoedUserServiceWithTwitterServiceResponse(_, Right(echoedUserService)) =>
                                            channel ! LocateWithTwitterServiceResponse(msg, Right(echoedUserService))
                                    }))
                            })
                    }))
            } catch { case e => error(e) }


        case msg @ Logout(echoedUserId) =>
            val channel = context.sender

            try {
                logger.debug("Processing logout for {}", echoedUserId)
                cache.remove(echoedUserId).cata(
                    eus => {
                        //eus.logout(echoedUserId)
                        channel ! LogoutResponse(msg, Right(true))
                        logger.debug("Logged out Echoed user {} ", echoedUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        logger.debug("Did not find EchoedUser to {}", msg)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(EchoedUserException("Could not logout Echoed user", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case msg @ CreateEchoedUserServiceWithId(echoedUserId) =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithIdResponse(msg, Left(EchoedUserException("Cannot get Echoed user service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating EchoedUserService with id {}", echoedUserId)
                Option(echoedUserDao.findById(echoedUserId)).cata(
                    echoedUser => {
                        val echoedUserService = new EchoedUserServiceActorClient(
                            context.actorOf(Props().withCreator {
                                val eu = echoedUserDao.findById(echoedUserId)
                                new EchoedUserServiceActor(
                                    echoedUser = eu,
                                    echoedUserDao = echoedUserDao,
                                    closetDao = closetDao,
                                    echoedFriendDao = echoedFriendDao,
                                    feedDao = feedDao,
                                    partnerSettingsDao = partnerSettingsDao,
                                    echoDao = echoDao,
                                    echoMetricsDao = echoMetricsDao,
                                    facebookServiceLocator = facebookServiceLocator,
                                    twitterServiceLocator = twitterServiceLocator)
                            }, echoedUserId))
                        channel ! CreateEchoedUserServiceWithIdResponse(msg, Right(echoedUserService))
                        cache.put(echoedUserId, echoedUserService)
                        logger.debug("Created EchoedUserService with id {}", echoedUserId)
                    },
                    {
                        channel ! CreateEchoedUserServiceWithIdResponse(msg, Left(EchoedUserNotFound(echoedUserId)))
                        logger.debug("Did not find an EchoedUser with id {}", echoedUserId)
                    })
            } catch { case e => error(e) }


        case msg @ CreateEchoedUserServiceWithFacebookService(facebookService) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithFacebookServiceResponse(
                        msg,
                        Left(new EchoedUserException("Cannot get Echoed user service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating EchoedUserService with {}", facebookService)
                facebookService.getFacebookUser().onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetFacebookUserResponse(_, Left(e)) => error(e)
                        case GetFacebookUserResponse(_, Right(facebookUser)) =>
                            logger.debug("Searching for Facebook User {}",facebookUser.id);
                            Option(echoedUserDao.findByFacebookUserId(facebookUser.id)) match {
                                case Some(echoedUser) =>
                                    logger.debug("Found {} with {}", echoedUser, facebookUser)
                                    (me ? CreateEchoedUserServiceWithId(echoedUser.id)).onComplete(_.fold(
                                        error(_),
                                        _ match {
                                            case CreateEchoedUserServiceWithIdResponse(_, Left(e)) => error(e)
                                            case CreateEchoedUserServiceWithIdResponse(_, Right(eus)) =>
                                                channel ! CreateEchoedUserServiceWithFacebookServiceResponse(msg, Right(eus))
                                        }))

                                case None =>
                                    logger.debug("Creating EchoedUser with {}", facebookUser)
                                    val echoedUser = new EchoedUser(facebookUser)
                                    echoedUserDao.insert(echoedUser)
                                    facebookService.assignEchoedUser(echoedUser)
                                    me.tell(msg, channel) //potential infinite loop if database never gets updated!?!?
                            }
                    }))
            } catch { case e => error(e) }


        case msg @ CreateEchoedUserServiceWithTwitterService(twitterService) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithTwitterServiceResponse(
                        msg,
                        Left(EchoedUserException("Could not create Echoed user service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating EchoedUserService with {}", twitterService)
                twitterService.getUser.onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetUserResponse(_, Left(e)) => error(e)
                        case GetUserResponse(_, Right(twitterUser)) => Option(echoedUserDao.findByTwitterUserId(twitterUser.id)).cata(
                            echoedUser => {
                                logger.debug("Found EchoedUser {} with TwitterUser {}", echoedUser, twitterUser)
                                (me ? CreateEchoedUserServiceWithId(echoedUser.id)).onComplete(_.fold(
                                    error(_),
                                    _ match {
                                        case CreateEchoedUserServiceWithIdResponse(_, Left(e)) => error(e)
                                        case CreateEchoedUserServiceWithIdResponse(_, Right(eus)) =>
                                            channel ! CreateEchoedUserServiceWithTwitterServiceResponse(msg, Right(eus))
                                    }))
                            },
                            {
                                logger.debug("Creating EchoedUser with {}", twitterUser)
                                val echoedUser = new EchoedUser(twitterUser)
                                echoedUserDao.insert(echoedUser)
                                twitterService.assignEchoedUser(echoedUser.id)
                                me.tell(msg, channel)
                            })
                    }
                ))
            } catch { case e => error(e) }

    }


    }), "EchoedUserService")

}
