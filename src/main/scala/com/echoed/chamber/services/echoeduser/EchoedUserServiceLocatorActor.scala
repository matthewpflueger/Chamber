package com.echoed.chamber.services.echoeduser

import scalaz._
import Scalaz._
import akka.actor._
import com.echoed.cache.{CacheListenerActorClient, CacheManager}
import scala.collection.mutable.ConcurrentMap
import akka.util.duration._
import akka.pattern.ask
import com.echoed.chamber.dao.views.{FeedDao, ClosetDao}
import com.echoed.chamber.dao._
import com.echoed.chamber.services.facebook.FacebookServiceLocator
import com.echoed.chamber.services.twitter.TwitterServiceLocator
import akka.actor.SupervisorStrategy.Restart
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import com.echoed.chamber.services.{EchoedActor, ActorClient}
import com.echoed.chamber.domain.EchoedUser
import scala.Right
import com.echoed.chamber.services.twitter.GetUserResponse
import akka.actor.OneForOneStrategy
import scala.Some
import com.echoed.cache.CacheEntryRemoved
import com.echoed.chamber.services.facebook.GetFacebookUserResponse
import scala.Left
import org.springframework.transaction.support.TransactionTemplate
import akka.util.Timeout

class EchoedUserServiceLocatorActor(
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        feedDao: FeedDao,
        partnerSettingsDao: PartnerSettingsDao,
        echoDao: EchoDao,
        echoedFriendDao: EchoedFriendDao,
        echoMetricsDao: EchoMetricsDao,
        partnerDao: PartnerDao,
        storyDao: StoryDao,
        chapterDao: ChapterDao,
        chapterImageDao: ChapterImageDao,
        imageDao: ImageDao,
        commentDao: CommentDao,
        transactionTemplate: TransactionTemplate,
        facebookServiceLocator: FacebookServiceLocator,
        twitterServiceLocator: TwitterServiceLocator,
        cacheManager: CacheManager,
        storyGraphUrl: String,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedActor {


    private var cache: ConcurrentMap[String, EchoedUserService] =
            cacheManager.getCache[EchoedUserService]("EchoedUserServices", Some(new CacheListenerActorClient(self)))


    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    def handle = {

        case msg @ CacheEntryRemoved(echoedUserId: String, echoedUserService: EchoedUserService, cause: String) =>
            log.debug("Received {}", msg)
            echoedUserService.logout(echoedUserId)
            log.debug("Sent logout for {}", echoedUserService)


        case msg @ LocateWithId(echoedUserId) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! LocateWithIdResponse(msg, Left(EchoedUserException("Could not locate Echoed user service", e)))
                log.error("Error processing {}: {}", msg, e)
            }

            try {
                log.debug("Locating EchoedUserService With id {}" , msg.echoedUserId)
                cache.get(echoedUserId) match {
                    case Some(echoedUserService) =>
                        channel ! LocateWithIdResponse(msg, Right(echoedUserService))
                        log.debug("Cache hit for EchoedUserService key {}", echoedUserId)
                    case _ =>
                        log.debug("Cache miss for EchoedUserService key {}", echoedUserId)
                        (me ? CreateEchoedUserServiceWithId(echoedUserId)).onComplete(_.fold(
                            e => error(e),
                            _ match {
                                case CreateEchoedUserServiceWithIdResponse(_, Left(e @ EchoedUserNotFound(id, _))) =>
                                    log.debug("EchoedUser {} not found", id)
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
                log.error("Error processing {}: {}", msg, e)
            }

            try {
                facebookService.getFacebookUser.onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetFacebookUserResponse(_, Left(e)) => error(e)
                        case GetFacebookUserResponse(_, Right(fu)) => Option(fu.echoedUserId).flatMap(cache.get(_)).cata(
                            echoedUserService => {
                                channel ! LocateWithFacebookServiceResponse(msg, Right(echoedUserService))
                                log.debug("Cache hit for EchoedUserService for {}", facebookService)
                            },
                            {
                                log.debug("Cache miss for EchoedUserService for {}", facebookService)
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
                log.error("Error processing {}: {}", msg, e)
            }

            try {
                twitterService.getUser.onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetUserResponse(_, Left(e)) => error(e)
                        case GetUserResponse(_, Right(tu)) => Option(tu.echoedUserId).flatMap(cache.get(_)).cata(
                            echoedUserService => {
                                channel ! LocateWithTwitterServiceResponse(msg, Right(echoedUserService))
                                log.debug("Cache hit for EchoedUserService {} for {} ", tu.echoedUserId, twitterService)
                            },
                            {
                                log.debug("Cache miss for EchoedUserService for {}", twitterService)
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
                log.debug("Processing logout for {}", echoedUserId)
                cache.remove(echoedUserId).cata(
                    eus => {
                        //eus.logout(echoedUserId)
                        channel ! LogoutResponse(msg, Right(true))
                        log.debug("Logged out Echoed user {} ", echoedUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        log.debug("Did not find EchoedUser to {}", msg)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(EchoedUserException("Could not logout Echoed user", e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
            }

        case msg @ CreateEchoedUserServiceWithId(echoedUserId) =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithIdResponse(msg, Left(EchoedUserException("Cannot get Echoed user service", e)))
                log.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                log.debug("Creating EchoedUserService with id {}", echoedUserId)
                cache.get(echoedUserId) match {
                    case Some(echoedUserService) =>
                        channel ! CreateEchoedUserServiceWithIdResponse(msg, Right(echoedUserService))
                        log.debug("Cache hit for EchoedUserService key {}", echoedUserId)
                    case _ =>
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
                                            partnerDao = partnerDao,
                                            echoMetricsDao = echoMetricsDao,
                                            storyDao = storyDao,
                                            chapterDao = chapterDao,
                                            chapterImageDao = chapterImageDao,
                                            commentDao = commentDao,
                                            imageDao = imageDao,
                                            transactionTemplate = transactionTemplate,
                                            facebookServiceLocator = facebookServiceLocator,
                                            twitterServiceLocator = twitterServiceLocator,
                                            storyGraphUrl = storyGraphUrl)
                                    }, echoedUserId))
                                channel ! CreateEchoedUserServiceWithIdResponse(msg, Right(echoedUserService))
                                cache.put(echoedUserId, echoedUserService)
                                log.debug("Created EchoedUserService with id {}", echoedUserId)
                            },
                            {
                                channel ! CreateEchoedUserServiceWithIdResponse(msg, Left(EchoedUserNotFound(echoedUserId)))
                                log.debug("Did not find an EchoedUser with id {}", echoedUserId)
                            })
                }
            } catch { case e => error(e) }


        case msg @ CreateEchoedUserServiceWithFacebookService(facebookService) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithFacebookServiceResponse(
                        msg,
                        Left(new EchoedUserException("Cannot get Echoed user service", e)))
                log.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                log.debug("Creating EchoedUserService with {}", facebookService)
                facebookService.getFacebookUser().onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetFacebookUserResponse(_, Left(e)) => error(e)
                        case GetFacebookUserResponse(_, Right(facebookUser)) =>
                            log.debug("Searching for Facebook User {}",facebookUser.id);
                            Option(echoedUserDao.findByFacebookUserId(facebookUser.id)) match {
                                case Some(echoedUser) =>
                                    log.debug("Found {} with {}", echoedUser, facebookUser)
                                    (me ? CreateEchoedUserServiceWithId(echoedUser.id)).onComplete(_.fold(
                                        error(_),
                                        _ match {
                                            case CreateEchoedUserServiceWithIdResponse(_, Left(e)) => error(e)
                                            case CreateEchoedUserServiceWithIdResponse(_, Right(eus)) =>
                                                channel ! CreateEchoedUserServiceWithFacebookServiceResponse(msg, Right(eus))
                                        }))

                                case None =>
                                    log.debug("Creating EchoedUser with {}", facebookUser)
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
                log.error("Unexpected error processing {}: {}", msg, e)
            }

            try {
                log.debug("Creating EchoedUserService with {}", twitterService)
                twitterService.getUser.onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetUserResponse(_, Left(e)) => error(e)
                        case GetUserResponse(_, Right(twitterUser)) => Option(echoedUserDao.findByTwitterUserId(twitterUser.id)).cata(
                            echoedUser => {
                                log.debug("Found EchoedUser {} with TwitterUser {}", echoedUser, twitterUser)
                                (me ? CreateEchoedUserServiceWithId(echoedUser.id)).onComplete(_.fold(
                                    error(_),
                                    _ match {
                                        case CreateEchoedUserServiceWithIdResponse(_, Left(e)) => error(e)
                                        case CreateEchoedUserServiceWithIdResponse(_, Right(eus)) =>
                                            channel ! CreateEchoedUserServiceWithTwitterServiceResponse(msg, Right(eus))
                                    }))
                            },
                            {
                                log.debug("Creating EchoedUser with {}", twitterUser)
                                val echoedUser = new EchoedUser(twitterUser)
                                echoedUserDao.insert(echoedUser)
                                twitterService.assignEchoedUser(echoedUser.id)
                                me.tell(msg, channel)
                            })
                    }
                ))
            } catch { case e => error(e) }

        case msg @ EchoedUserIdentifiable(echoedUserId) =>
            val me = context.self
            val channel = context.sender

            log.debug("Starting to locate EchoedUser {}", echoedUserId)

            val constructor = findResponseConstructor(msg)

            (me ? LocateWithId(echoedUserId)).mapTo[LocateWithIdResponse].onComplete(_.fold(
                e => {
                    log.error("Unexpected error in locating EchoedUser {}: {}", echoedUserId, e)
                    channel ! constructor.newInstance(msg, Left(new EchoedUserException("Error locating echoed user %s" format echoedUserId, e)))
                },
                _ match {
                    case LocateWithIdResponse(LocateWithId(echoedUserId), Left(e)) =>
                        log.error("Error locating EchoedUser {}: {}", echoedUserId, e)
                        channel ! constructor.newInstance(msg, Left(e))
                    case LocateWithIdResponse(_, Right(eus)) =>
                        log.debug("Located EchoedUser {}, forwarding on message {}", echoedUserId, msg)
                        eus.asInstanceOf[ActorClient].actorRef.tell(msg, channel)
                }))


    }

    private def findResponseConstructor(msg: EchoedUserMessage) = {
        val requestClass = msg.getClass
        val responseClass = Thread.currentThread.getContextClassLoader.loadClass(requestClass.getName + "Response")
        responseClass.getConstructor(requestClass, classOf[Either[_, _]])
    }

}
