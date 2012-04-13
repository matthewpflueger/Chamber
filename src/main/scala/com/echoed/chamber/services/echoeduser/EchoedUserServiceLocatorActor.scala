package com.echoed.chamber.services.echoeduser

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import akka.actor._
import com.echoed.chamber.services.twitter.GetUserResponse
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import scala.collection.mutable.ConcurrentMap
import com.echoed.chamber.services.facebook.GetFacebookUserResponse

class EchoedUserServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceLocatorActor])

    @BeanProperty var echoedUserServiceCreator: EchoedUserServiceCreator = _
    @BeanProperty var cacheManager: CacheManager = _


    private var cache: ConcurrentMap[String, EchoedUserService] = null


    override def preStart() {
        cache = cacheManager.getCache[EchoedUserService]("EchoedUserServices", Some(new CacheListenerActorClient(self)))
    }

    def cacheEchoedUserService(msg: EchoedUserMessage, echoedUserService: EchoedUserService) {
        def error(e: Throwable) {
            logger.error("Failed to cache EchoedUserService for %s" format msg, e)
        }

        echoedUserService.getEchoedUser.onComplete(_.value.get.fold(
            e => error(e),
            _ match {
                case GetEchoedUserResponse(_, Left(e)) => error(e)
                case GetEchoedUserResponse(_, Right(echoedUser)) =>
                    cache.put(echoedUser.id, echoedUserService)
                    logger.debug("Seeded cache with EchoedUserService {} for {}", echoedUser.id, msg)
            }))
    }

    def receive = {

        case msg @ CacheEntryRemoved(echoedUserId: String, echoedUserService: EchoedUserService, cause: String) =>
            logger.debug("Received {}", msg)
            echoedUserService.logout(echoedUserId)
            logger.debug("Sent logout for {}", echoedUserService)

        case msg @ LocateWithId(echoedUserId) =>
            val channel: Channel[LocateWithIdResponse] = self.channel

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
                        echoedUserServiceCreator.createEchoedUserServiceUsingId(echoedUserId).onComplete(_.value.get.fold(
                            e => error(e),
                            _ match {
                                case CreateEchoedUserServiceWithIdResponse(_, Left(e @ EchoedUserNotFound(id, _))) =>
                                    logger.debug("EchoedUser {} not found", id)
                                    channel ! LocateWithIdResponse(msg, Left(e))
                                case CreateEchoedUserServiceWithIdResponse(_, Left(e)) => error(e)
                                case CreateEchoedUserServiceWithIdResponse(_, Right(echoedUserService)) =>
                                    channel ! LocateWithIdResponse(msg, Right(echoedUserService))
                                    cache.put(echoedUserId, echoedUserService)
                                    logger.debug("Seeded cache with EchoedUserService key {}", echoedUserId)
                            }
                        ))
                }
            } catch { case e => error(e) }


        case msg @ LocateWithFacebookService(facebookService) =>
            val channel: Channel[LocateWithFacebookServiceResponse] = self.channel

            def error(e: Throwable) {
                LocateWithFacebookServiceResponse(msg, Left(EchoedUserException("Could not locate Echoed user service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                facebookService.getFacebookUser.onComplete(_.value.get.fold(
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
                                echoedUserServiceCreator.createEchoedUserServiceUsingFacebookService(facebookService).onComplete(_.value.get.fold(
                                    e => error(e),
                                    _ match {
                                        case CreateEchoedUserServiceWithFacebookServiceResponse(_, Left(e)) => error(e)
                                        case CreateEchoedUserServiceWithFacebookServiceResponse(_, Right(echoedUserService))=>
                                            channel ! LocateWithFacebookServiceResponse(msg, Right(echoedUserService))
                                            cacheEchoedUserService(msg, echoedUserService)
                                    }))
                            })
                    }))
            } catch { case e => error(e) }


        case msg @ LocateWithTwitterService(twitterService) =>
            val channel: Channel[LocateWithTwitterServiceResponse] = self.channel

            def error(e: Throwable) {
                channel ! LocateWithTwitterServiceResponse(msg, Left(EchoedUserException("Could not locate Echoed user service", e)))
                logger.error("Error processing %s", msg, e)
            }

            try {
                twitterService.getUser.onComplete(_.value.get.fold(
                    e => error(e),
                    _ match {
                        case GetUserResponse(_, Left(e)) => error(e)
                        case GetUserResponse(_, Right(tu)) => Option(tu.echoedUserId).flatMap(cache.get(_)).cata(
                            echoedUserService => {
                                channel ! LocateWithTwitterServiceResponse(msg, Right(echoedUserService))
                                logger.debug("Cache hit for EchoedUserService {} for {} ", tu.echoedUserId, twitterService)
                            },
                            {
                                logger.debug("Cache miss for EchoedUserService for {}", twitterService)
                                echoedUserServiceCreator.createEchoedUserServiceUsingTwitterService(twitterService).onComplete(_.value.get.fold(
                                    e => error(e),
                                    _ match {
                                        case CreateEchoedUserServiceWithTwitterServiceResponse(_, Left(e)) => error(e)
                                        case CreateEchoedUserServiceWithTwitterServiceResponse(_, Right(echoedUserService)) =>
                                            channel ! LocateWithTwitterServiceResponse(msg, Right(echoedUserService))
                                            cacheEchoedUserService(msg, echoedUserService)
                                    }))

                            })
                    }))
            } catch { case e => error(e) }


        case msg @ Logout(echoedUserId) =>
            val channel: Channel[LogoutResponse] = self.channel

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
    }


}
