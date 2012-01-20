package com.echoed.chamber.services.echoeduser

import reflect.BeanProperty
import org.slf4j.LoggerFactory

import com.echoed.chamber.services.facebook.{FacebookServiceLocator, FacebookService}
import akka.dispatch.Future
import com.echoed.chamber.dao.views.{ClosetDao, FeedDao}
import com.echoed.chamber.dao._
import akka.actor.{Channel, Actor}
import scalaz._
import Scalaz._
import com.echoed.chamber.services.twitter.{GetTwitterServiceWithIdResponse, GetUserResponse, TwitterService, TwitterServiceLocator}
import com.echoed.chamber.domain.{FacebookUser, EchoedUser}
import org.springframework.beans.factory.annotation.Required


class EchoedUserServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceCreatorActor])


    @BeanProperty var echoedUserDao: EchoedUserDao = _
    @BeanProperty var closetDao: ClosetDao = _
    @BeanProperty var feedDao: FeedDao = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoedFriendDao: EchoedFriendDao = _
    @BeanProperty var echoMetricsDao: EchoMetricsDao = _

    @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _
    @BeanProperty var twitterServiceLocator: TwitterServiceLocator = _



    def receive = {
        case msg @ CreateEchoedUserServiceWithId(echoedUserId) =>
            val channel: Channel[CreateEchoedUserServiceWithIdResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithIdResponse(msg, Left(EchoedUserException("Cannot get Echoed user service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating EchoedUserService with id {}", echoedUserId)
                Option(echoedUserDao.findById(msg.echoedUserId)).cata(
                    echoedUser => {
                        val echoedUserService = createEchoedUserService(
                                echoedUser,
                                facebookServiceLocator,
                                twitterServiceLocator)
                        channel ! CreateEchoedUserServiceWithIdResponse(msg, Right(echoedUserService))
                        logger.debug("Created EchoedUserService with id {}", echoedUserId)
                    },
                    {
                        channel ! CreateEchoedUserServiceWithIdResponse(msg, Left(EchoedUserNotFound(echoedUserId)))
                        logger.debug("Did not find an EchoedUser with id {}", echoedUserId)
                    })
            } catch { case e => error(e) }


        case msg @ CreateEchoedUserServiceWithFacebookService(facebookService) =>
            val channel: Channel[CreateEchoedUserServiceWithFacebookServiceResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithFacebookServiceResponse(
                        msg,
                        Left(new EchoedUserException("Cannot get Echoed user service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating EchoedUserService with {}", facebookService)
                msg.facebookService.getFacebookUser().onComplete(_.value.get.fold(
                    e => error(e),
                    _ match {
                        case facebookUser: FacebookUser =>
                            logger.debug("Searching for Facebook User {}",facebookUser.id);
                            Option(echoedUserDao.findByFacebookUserId(facebookUser.id)) match {
                                case Some(echoedUser) =>
                                    logger.debug("Found {} with {}", echoedUser, facebookUser)
                                    val echoedUserServiceActor = createEchoedUserService(
                                            echoedUser,
                                            facebookServiceLocator,
                                            twitterServiceLocator)
                                    channel ! CreateEchoedUserServiceWithFacebookServiceResponse(msg, Right(echoedUserServiceActor))
                                case None =>
                                    logger.debug("Creating EchoedUser with {}", facebookUser)
                                    val echoedUser = new EchoedUser(facebookUser)
                                    echoedUserDao.insert(echoedUser)
                                    facebookService.assignEchoedUser(echoedUser)
                                    val echoedUserServiceActor = createEchoedUserService(
                                            echoedUser,
                                            facebookServiceLocator,
                                            twitterServiceLocator)
                                    channel ! CreateEchoedUserServiceWithFacebookServiceResponse(msg, Right(echoedUserServiceActor))
                            }
                    }
                ))
            } catch { case e => error(e) }


        case msg @ CreateEchoedUserServiceWithTwitterService(twitterService) =>
            val channel: Channel[CreateEchoedUserServiceWithTwitterServiceResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithTwitterServiceResponse(
                        msg,
                        Left(EchoedUserException("Could not create Echoed user service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating EchoedUserService with {}", twitterService)
                twitterService.getUser.onComplete(_.value.get.fold(
                    e => error(e),
                    _ match {
                        case GetUserResponse(_, Left(e)) => error(e)
                        case GetUserResponse(_, Right(twitterUser)) => Option(echoedUserDao.findByTwitterUserId(twitterUser.id)).cata(
                            echoedUser => {
                                logger.debug("Found EchoedUser {} with TwitterUser {}", echoedUser, twitterUser)
                                channel ! CreateEchoedUserServiceWithTwitterServiceResponse(
                                        msg, Right(createEchoedUserService(echoedUser, facebookServiceLocator, twitterServiceLocator)))
                            },
                            {
                                logger.debug("Creating EchoedUser with {}", twitterUser)
                                val echoedUser = new EchoedUser(twitterUser)
                                echoedUserDao.insert(echoedUser)
                                twitterService.assignEchoedUser(echoedUser.id)
                                channel ! CreateEchoedUserServiceWithTwitterServiceResponse(
                                        msg,
                                        Right(createEchoedUserService(echoedUser, facebookServiceLocator, twitterServiceLocator)))
                            })
                    }
                ))
            } catch { case e => error(e) }
    }


    private def createEchoedUserService(
            echoedUser: EchoedUser,
            facebookServiceLocator: FacebookServiceLocator,
            twitterServiceLocator: TwitterServiceLocator) =
        new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser = echoedUser,
                            echoedUserDao = echoedUserDao,
                            closetDao = closetDao,
                            echoedFriendDao = echoedFriendDao,
                            feedDao = feedDao,
                            echoPossibilityDao = echoPossibilityDao,
                            retailerSettingsDao = retailerSettingsDao,
                            echoDao = echoDao,
                            echoMetricsDao = echoMetricsDao,
                            facebookServiceLocator = facebookServiceLocator,
                            twitterServiceLocator = twitterServiceLocator)).start)

}
