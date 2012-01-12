package com.echoed.chamber.services.echoeduser

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.EchoedUser

import com.echoed.chamber.services.facebook.{FacebookServiceLocator, FacebookService}
import akka.dispatch.Future
import com.echoed.chamber.dao.views.{ClosetDao, FeedDao}
import com.echoed.chamber.dao._
import akka.actor.{Channel, Actor}
import scalaz._
import Scalaz._
import com.echoed.chamber.services.twitter.{GetUserResponse, TwitterService, TwitterServiceLocator}


class EchoedUserServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceCreatorActor])


    @BeanProperty var echoedUserDao: EchoedUserDao = _
    @BeanProperty var closetDao: ClosetDao = _
    @BeanProperty var feedDao: FeedDao = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoedFriendDao: EchoedFriendDao = _

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
                        val echoedUserService = createEchoedUserService(echoedUser)
                        channel ! CreateEchoedUserServiceWithIdResponse(msg, Right(echoedUserService))

                        logger.debug("Created EchoedUserService with id {}", echoedUserId)

                        Option(echoedUser.facebookUserId).map {
                            facebookServiceLocator.getFacebookServiceWithFacebookUserId(_).onResult {
                                case facebookService: FacebookService =>
                                    echoedUserService.assignFacebookService(facebookService)
                            }
                        }

                        Option(echoedUser.twitterUserId).map {
                            twitterServiceLocator.getTwitterServiceWithId(_).onResult {
                                case twitterService: TwitterService =>
                                    echoedUserService.assignTwitterService(twitterService)
                            }
                        }
                    },
                    {
                        channel ! CreateEchoedUserServiceWithIdResponse(msg, Left(EchoedUserNotFound(echoedUserId)))
                        logger.debug("Did not find an EchoedUser with id {}", echoedUserId)
                    })
            } catch { case e => error(e) }


        case msg @ CreateEchoedUserServiceWithFacebookService(facebookService) =>
            logger.debug("Creating EchoedUserService with {}", facebookService)
            val facebookUser = msg.facebookService.facebookUser.get
            logger.debug("Searching for Facebook User {}",facebookUser.id);
            Option(echoedUserDao.findByFacebookUserId(facebookUser.id)) match {
                case Some(echoedUser) =>
                    logger.debug("Found {} with {}", echoedUser, facebookUser)
                    val echoedUserServiceActor = createEchoedUserService(echoedUser, facebookService = Some(facebookService))
                    self.channel ! CreateEchoedUserServiceWithFacebookServiceResponse(msg, Right(echoedUserServiceActor))
                case None =>
                    logger.debug("Creating EchoedUser with {}", facebookUser)
                    val echoedUser = new EchoedUser(facebookUser)
                    echoedUserDao.insert(echoedUser)
                    facebookService.assignEchoedUser(echoedUser)
                    val echoedUserServiceActor =  createEchoedUserService(echoedUser, facebookService = Some(facebookService))
                    self.channel ! CreateEchoedUserServiceWithFacebookServiceResponse(msg, Right(echoedUserServiceActor))
            }


        case msg @ CreateEchoedUserServiceWithTwitterService(twitterService) =>
            val channel = self.channel

            def error(e: Throwable) {
                channel ! CreateEchoedUserServiceWithTwitterServiceResponse(
                        msg,
                        Left(EchoedUserException("Could not create Echoed user service", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating EchoedUserService with {}", twitterService)
                twitterService.getUser.onResult {
                    case GetUserResponse(_, Left(e)) => error(e)
                    case GetUserResponse(_, Right(twitterUser)) => Option(echoedUserDao.findByTwitterUserId(twitterUser.id)).cata(
                        echoedUser => {
                            logger.debug("Found EchoedUser {} with TwitterUser {}", echoedUser, twitterUser)
                            channel ! CreateEchoedUserServiceWithTwitterServiceResponse(
                                    msg, Right(createEchoedUserService(echoedUser, twitterService = Some(twitterService))))
                        },
                        {
                            logger.debug("Creating EchoedUser with {}", twitterUser)
                            val echoedUser = new EchoedUser(twitterUser)
                            echoedUserDao.insert(echoedUser)
                            twitterService.assignEchoedUser(echoedUser.id)
                            channel ! CreateEchoedUserServiceWithTwitterServiceResponse(
                                    msg, Right(createEchoedUserService(echoedUser, twitterService = Some(twitterService))))
                        })
                }
            } catch { case e => error(e) }

    }


    private def createEchoedUserService(
            echoedUser: EchoedUser,
            facebookService: Option[FacebookService] = None,
            twitterService: Option[TwitterService] = None) =
        new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser = echoedUser,
                            echoedUserDao = echoedUserDao,
                            closetDao = closetDao,
                            echoedFriendDao = echoedFriendDao,
                            feedDao = feedDao,
                            echoPossibilityDao = echoPossibilityDao,
                            retailerSettingsDao = retailerSettingsDao,
                            echoDao = echoDao,
                            facebookService = facebookService,
                            twitterService = twitterService)).start)

}
