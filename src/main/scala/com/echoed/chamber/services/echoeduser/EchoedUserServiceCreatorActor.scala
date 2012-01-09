package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.services.twitter.TwitterServiceLocator

import com.echoed.chamber.services.facebook.{FacebookServiceLocator, FacebookService}
import akka.dispatch.Future
import com.echoed.chamber.dao.views.{ClosetDao, FeedDao}
import com.echoed.chamber.dao._


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
            logger.debug("Creating EchoedUserService with id {}", echoedUserId)
            Option(echoedUserDao.findById(msg.echoedUserId)) match{
                case Some(echoedUser) =>
                    val futureFacebookService = if (echoedUser.facebookUserId != null) {
                        facebookServiceLocator.getFacebookServiceWithFacebookUserId(echoedUser.facebookUserId)
                    } else{
                        Future[FacebookService] { null }
                    }

                    val futureTwitterService = if ( echoedUser.twitterUserId != null ) {
                        twitterServiceLocator.getTwitterServiceWithId(echoedUser.twitterUserId)
                    } else {
                        Future[TwitterService] { null }
                    }

                    val channel = self.channel
                    for {
                        facebookService <- futureFacebookService
                        twitterService <- futureTwitterService
                    } yield {
                        val echoedUserServiceActor = createEchoedUserService(
                                echoedUser,
                                facebookService = Some(facebookService),
                                twitterService = Some(twitterService))
                        channel ! CreateEchoedUserServiceWithIdResponse(msg, Right(echoedUserServiceActor))
                        logger.debug("Created EchoedUserService with id {}", msg.echoedUserId)
                    }

                case None =>
                    self.channel ! CreateEchoedUserServiceWithIdResponse(msg, Left(EchoedUserException("Did not find an EchoedUser")))
                    logger.warn("Did not find an EchoedUser with id {}", msg.echoedUserId)
            }


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
            logger.debug("Creating EchoedUserService with {}", twitterService)
            val twitterUser = twitterService.twitterUser.get
            Option(echoedUserDao.findByTwitterUserId(twitterUser.id)) match {
                case Some(echoedUser) =>
                    logger.debug("Found EchoedUser {} with TwitterUser {}", echoedUser, twitterUser)
                    val echoedUserServiceActor = createEchoedUserService(echoedUser, twitterService = Some(twitterService))
                    self.channel ! CreateEchoedUserServiceWithTwitterServiceResponse(msg, Right(echoedUserServiceActor))
                case None =>
                    logger.debug("Creating EchoedUser with {}", twitterUser)
                    val echoedUser = new EchoedUser(twitterUser)
                    echoedUserDao.insert(echoedUser)
                    twitterService.assignEchoedUserId(echoedUser.id)
                    val echoedUserServiceActor = createEchoedUserService(echoedUser, twitterService = Some(twitterService))
                    self.channel ! CreateEchoedUserServiceWithTwitterServiceResponse(msg, Right(echoedUserServiceActor))
            }

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
