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
import com.echoed.chamber.dao.{EchoedFriendDao, EchoedUserDao}


class EchoedUserServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceCreatorActor])


    @BeanProperty var echoedUserDao: EchoedUserDao = _
    @BeanProperty var closetDao: ClosetDao = _
    @BeanProperty var feedDao: FeedDao = _
    @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _
    @BeanProperty var twitterServiceLocator: TwitterServiceLocator = _
    @BeanProperty var echoedFriendDao: EchoedFriendDao = _


    def receive = {
        case msg: CreateEchoedUserServiceWithId => {
            logger.debug("Creating EchoedUserService with Id: {}", msg.echoedUserId)
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
                        val EchoedUserServiceActor = new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            facebookService,
                            twitterService)).start)
                        channel ! CreateEchoedUserServiceWithIdResponse(msg,Right(EchoedUserServiceActor))
                        logger.debug("Created EchoedUserService with id {}", msg.echoedUserId)
                    }
                case None =>
                    val channel = self.channel
                    channel ! CreateEchoedUserServiceWithIdResponse(msg,Left(EchoedUserException("Did not find an EchoedUser")))
                    logger.warn("Did not find an EchoedUser with id {}", msg.echoedUserId)
                    throw new RuntimeException("No EchoedUser with id %s" format msg.echoedUserId)
            }
        }

        case msg: CreateEchoedUserServiceWithFacebookService  => {
            val facebookService = msg.facebookService
            logger.debug("Creating EchoedUserService with {}", facebookService)
            val facebookUser = msg.facebookService.facebookUser.get
            logger.debug("Searching for Facebook User {}",facebookUser.id);
            Option(echoedUserDao.findByFacebookUserId(facebookUser.id)) match {
                case Some(echoedUser) =>
                    logger.debug("Found {} with {}", echoedUser, facebookUser)
                    val EchoedUserServiceActor = new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            facebookService)).start)
                    self.channel ! CreateEchoedUserServiceWithFacebookServiceResponse(msg,Right(EchoedUserServiceActor))
                case None =>
                    logger.debug("Creating EchoedUser with {}", facebookUser)
                    val echoedUser = new EchoedUser(facebookUser)
                    echoedUserDao.insert(echoedUser)
                    facebookService.assignEchoedUser(echoedUser)
                    val EchoedUserServiceActor =  new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            facebookService)).start)
                    self.channel ! CreateEchoedUserServiceWithFacebookServiceResponse(msg,Right(EchoedUserServiceActor))
            }
        }

        case msg: CreateEchoedUserServiceWithTwitterService => {
            val twitterService = msg.twitterService
            logger.debug("Creating EchoedUserService with {}", twitterService)
            val twitterUser = twitterService.twitterUser.get
            Option(echoedUserDao.findByTwitterUserId(twitterUser.id)) match {
                case Some(echoedUser) =>
                    logger.debug("Found EchoedUser {} with TwitterUser {}", echoedUser, twitterUser)
                    val EchoedUserServiceActor = new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            twitterService)).start)
                    self.channel ! CreateEchoedUserServiceWithTwitterServiceResponse(msg,Right(EchoedUserServiceActor))
                case None =>
                    logger.debug("Creating EchoedUser with {}", twitterUser)
                    val echoedUser = new EchoedUser(twitterUser)
                    echoedUserDao.insert(echoedUser)
                    twitterService.assignEchoedUserId(echoedUser.id)
                    val EchoedUserServiceActor = new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            twitterService)).start)
                    self.channel ! CreateEchoedUserServiceWithTwitterServiceResponse(msg,Right(EchoedUserServiceActor))
            }
        }



        case ("id", id: String) => {
            logger.debug("Creating EchoedUserService using id {}", id)

            Option(echoedUserDao.findById(id)) match {
                case Some(echoedUser) =>

                    val futureFacebookService = if (echoedUser.facebookUserId != null) {
                        facebookServiceLocator.getFacebookServiceWithFacebookUserId(echoedUser.facebookUserId)
                    } else {
                        Future[FacebookService] { null }
                    }

                    val futureTwitterService = if (echoedUser.twitterUserId != null) {
                        twitterServiceLocator.getTwitterServiceWithId(echoedUser.twitterUserId)
                    } else {
                        Future[TwitterService] { null }
                    }

                    val channel = self.channel
                    for {
                        facebookService <- futureFacebookService
                        twitterService <- futureTwitterService
                    } yield {
                        channel ! new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                                echoedUser,
                                echoedUserDao,
                                closetDao,
                                echoedFriendDao,
                                feedDao,
                                facebookService,
                                twitterService)).start)
                        logger.debug("Created EchoedUserService with id {}", id)
                    }

                case None =>
                    logger.warn("Did not find an EchoedUser with id {}", id)
                    throw new RuntimeException("No EchoedUser with id %s" format id)
            }
        }
        case ("facebookService", facebookService: FacebookService) => {
            logger.debug("Creating EchoedUserService with {}", facebookService)
            val facebookUser = facebookService.facebookUser.get
            logger.debug("Searching for Facebook User {}",facebookUser.id);
            Option(echoedUserDao.findByFacebookUserId(facebookUser.id)) match {
                case Some(echoedUser) =>
                    logger.debug("Found {} with {}", echoedUser, facebookUser)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            facebookService)).start)
                case None =>
                    logger.debug("Creating EchoedUser with {}", facebookUser)
                    val echoedUser = new EchoedUser(facebookUser)
                    echoedUserDao.insert(echoedUser)
                    facebookService.assignEchoedUser(echoedUser)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            facebookService)).start)

            }
        }

        case ("twitterService", twitterService:TwitterService) => {
            logger.debug("Creating EchoedUserService with {}", twitterService)
            val twitterUser = twitterService.twitterUser.get
            Option(echoedUserDao.findByTwitterUserId(twitterUser.id)) match {
                case Some(echoedUser) =>
                    logger.debug("Found EchoedUser {} with TwitterUser {}", echoedUser, twitterUser)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            twitterService)).start)
                case None =>
                    logger.debug("Creating EchoedUser with {}", twitterUser)
                    val echoedUser = new EchoedUser(twitterUser)
                    echoedUserDao.insert(echoedUser)
                    twitterService.assignEchoedUserId(echoedUser.id)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(new EchoedUserServiceActor(
                            echoedUser,
                            echoedUserDao,
                            closetDao,
                            echoedFriendDao,
                            feedDao,
                            twitterService)).start)
            }
        }
    }

    private def locateTwitterService(twitterUserId: String) = {
        val twitterService:TwitterService = twitterServiceLocator.getTwitterServiceWithId(twitterUserId).get.asInstanceOf[TwitterService]
        twitterService
    }

    private def locateFacebookService(twitterFacebookId:String) = {
        val facebookService:FacebookService = null
        facebookService
    }
}
