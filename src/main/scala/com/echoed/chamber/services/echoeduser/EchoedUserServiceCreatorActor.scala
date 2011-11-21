package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.services.facebook.FacebookServiceLocator
import com.echoed.chamber.services.twitter.TwitterServiceLocator

import com.echoed.chamber.services.facebook.{FacebookServiceLocator, FacebookService}
import scalaz._
import Scalaz._
import akka.dispatch.Future
import com.echoed.chamber.dao.views.ClosetDao


class EchoedUserServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceCreatorActor])


    @BeanProperty var echoedUserDao: EchoedUserDao = _
    @BeanProperty var closetDao: ClosetDao = _
    @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _
    @BeanProperty var twitterServiceLocator: TwitterServiceLocator = _


    def receive = {
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
                        channel ! new EchoedUserServiceActorClient(Actor.actorOf(
                                new EchoedUserServiceActor(echoedUser, echoedUserDao, closetDao, facebookService, twitterService)).start)
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
            Option(echoedUserDao.findByFacebookUserId(facebookUser.id)) match {
                case Some(echoedUser) =>
                    logger.debug("Found {} with {}", echoedUser, facebookUser)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(
                            new EchoedUserServiceActor(echoedUser, echoedUserDao, closetDao, facebookService)).start)
                case None =>
                    logger.debug("Creating EchoedUser with {}", facebookUser)
                    val echoedUser = new EchoedUser(facebookUser)
                    echoedUserDao.insert(echoedUser)
                    facebookService.assignEchoedUser(echoedUser)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(
                            new EchoedUserServiceActor(echoedUser, echoedUserDao, closetDao, facebookService)).start)

            }
        }

        case ("twitterService", twitterService:TwitterService) => {
            logger.debug("Creating EchoedUserService with {}", twitterService)
            val twitterUser = twitterService.twitterUser.get
            Option(echoedUserDao.findByTwitterUserId(twitterUser.id)) match {
                case Some(echoedUser) =>
                    logger.debug("Found EchoedUser {} with TwitterUser {}", echoedUser, twitterUser)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(
                            new EchoedUserServiceActor(echoedUser, echoedUserDao, closetDao, twitterService)).start)
                case None =>
                    logger.debug("Creating EchoedUser with {}", twitterUser)
                    val echoedUser = new EchoedUser(twitterUser)
                    echoedUserDao.insert(echoedUser)
                    twitterService.assignEchoedUserId(echoedUser.id)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(
                            new EchoedUserServiceActor(echoedUser, echoedUserDao, closetDao, twitterService)).start)
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
