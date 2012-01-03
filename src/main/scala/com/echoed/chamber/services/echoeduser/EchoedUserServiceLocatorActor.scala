package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import collection.mutable.WeakHashMap
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.services.EchoedException

class EchoedUserServiceLocatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceLocatorActor])

    @BeanProperty var echoedUserServiceCreator: EchoedUserServiceCreator = null

    private val cache = WeakHashMap[String, EchoedUserService]()
    private val cacheFacebookService = WeakHashMap[FacebookService, EchoedUserService]()
    private val cacheTwitterService = WeakHashMap[TwitterService,EchoedUserService]()

    def receive = {

        case msg: LocateWithId =>
            logger.debug("Locating EchoedUserService With id {}" , msg.echoedUserId)
            val channel = self.channel
            cache.get(msg.echoedUserId) match {
                case Some(echoedUserService) =>
                    channel ! LocateWithIdResponse(msg,Right(echoedUserService))
                    logger.debug("Cache hit for EchoedUserService key {}" ,msg.echoedUserId)
                case _ =>
                    logger.debug("Cache miss for EchoedUserService key {}", msg.echoedUserId)
                    echoedUserServiceCreator.createEchoedUserServiceUsingId(msg.echoedUserId).onResult{
                        case CreateEchoedUserServiceWithIdResponse(_,Left(error)) =>
                            logger.error("Error Creating EchoedUserService: {}", error)
                            channel ! LocateWithIdResponse(msg,Left(error))
                        case CreateEchoedUserServiceWithIdResponse(_,Right(echoedUserService)) =>
                            channel ! LocateWithIdResponse(msg,Right(echoedUserService))
                            cache += (msg.echoedUserId -> echoedUserService)
                            logger.debug("Seeded cache with EchoedUserService key {}", msg.echoedUserId)
                    }
                    .onException{
                        case e=>
                            logger.error("Exception thrown Creating EchoedUserService: {}", e)
                            channel ! LocateWithIdResponse(msg,Left(EchoedUserException(cause = e)))
                    }
            }


        case msg: LocateWithFacebookService =>
            val channel = self.channel
            cacheFacebookService.get(msg.facebookService) match {
                case Some(echoedUserService) =>
                    channel ! LocateWithFacebookServiceResponse(msg,Right(echoedUserService))
                    logger.debug("Cache hit for EchoedUserService with {}", msg.facebookService)
                case _=>
                    logger.debug("Cache miss for EchoedUserService with {}", msg.facebookService)
                    echoedUserServiceCreator.createEchoedUserServiceUsingFacebookService( msg.facebookService).onResult {
                        case CreateEchoedUserServiceWithFacebookServiceResponse(_,Left(error)) =>
                            logger.error("Error Creating EchoedUserService with FacebookService: {}", error)
                            LocateWithFacebookServiceResponse(msg,Left(error))
                        case CreateEchoedUserServiceWithFacebookServiceResponse(_,Right(echoedUserService))=>
                            channel ! LocateWithFacebookServiceResponse(msg,Right(echoedUserService))
                            cacheFacebookService += (msg.facebookService -> echoedUserService)
                            logger.debug("Seeded cacheFacebookService with EchoedUserService key {}", msg.facebookService)
                    }
                    .onException{
                        case e =>
                            logger.error("Exception thrown Creating EchoedUserService: {}", e)
                            channel ! LocateWithFacebookServiceResponse(msg,Left(EchoedUserException(cause = e)))
                    }
            }


        case msg: LocateWithTwitterService =>
            val channel = self.channel
            cacheTwitterService.get(msg.twitterService) match{
                case Some(echoedUserService) =>
                    channel ! LocateWithTwitterServiceResponse(msg,Right(echoedUserService))
                    logger.debug("Cache hit for EchoedUserService with {} ", msg.twitterService)
                case _=>
                    logger.debug("Cache miss for EchoedUserService with {}", msg.twitterService)
                    echoedUserServiceCreator.createEchoedUserServiceUsingTwitterService(msg.twitterService).onResult{
                        case CreateEchoedUserServiceWithTwitterServiceResponse(_,Left(error)) =>
                            logger.error("Error creating EchoedUserService: {}", error)
                            channel ! LocateWithTwitterServiceResponse(msg,Left(error))
                        case CreateEchoedUserServiceWithTwitterServiceResponse(_,Right(echoedUserService)) =>
                            channel ! LocateWithTwitterServiceResponse(msg,Right(echoedUserService))
                            cacheTwitterService +=(msg.twitterService -> echoedUserService)
                            logger.debug("Seeded cacheTwitterService with EchoedUserService key{}", msg.twitterService)
                    }
                    .onException{
                        case e=>
                            logger.error("Exception thrown Locating EchoedUserService: {} ", e)
                            channel ! LocateWithTwitterServiceResponse(msg,Left(EchoedUserException(cause = e)))
                    }
            }

    }


}
