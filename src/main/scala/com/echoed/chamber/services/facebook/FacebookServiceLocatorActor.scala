package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import akka.actor.{Channel, Actor}
import com.echoed.cache.{CacheEntryRemoved, CacheManager, CacheListenerActorClient}
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._


class FacebookServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[FacebookServiceLocatorActor])

    @BeanProperty var facebookServiceCreator: FacebookServiceCreator = _
    @BeanProperty var cacheManager: CacheManager = _


    private var cache: ConcurrentMap[String, FacebookService] = null
    private val cacheByFacebookId: ConcurrentMap[String, FacebookService] = new ConcurrentHashMap[String, FacebookService]()


    override def preStart() {
        cache = cacheManager.getCache[FacebookService]("FacebookServices", Some(new CacheListenerActorClient(self)))
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
            val channel: Channel[LocateByCodeResponse] = self.channel

            def error(e: Throwable) {
                channel ! LocateByCodeResponse(msg, Left(FacebookException("Could not locate Facebook user", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Locating FacebookService with code {}", code)
                facebookServiceCreator.createFromCode(code, queryString).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case CreateFromCodeResponse(_, Left(e)) => error(e)
                        case CreateFromCodeResponse(_, Right(facebookService)) =>
                            channel ! LocateByCodeResponse(msg, Right(facebookService))
                            facebookService.getFacebookUser.onComplete(_.value.get.fold(
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
            val channel: Channel[LocateByFacebookIdResponse] = self.channel

            def error(e: Throwable) {
                channel ! LocateByFacebookIdResponse(msg, Left(FacebookException("Could not locate Facebook service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            def updateCache(facebookService: FacebookService) {
                facebookService.getFacebookUser.onComplete(_.value.get.fold(
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
                        facebookServiceCreator.createFromFacebookId(facebookId, accessToken).onComplete(_.value.get.fold(
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
            val channel: Channel[LocateByIdResponse] = self.channel

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
                        facebookServiceCreator.createFromId(facebookUserId).onComplete(_.value.get.fold(
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
            val channel: Channel[LogoutResponse] = self.channel

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
    }


}
