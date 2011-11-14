package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.facebook.FacebookService


class EchoedUserServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceCreatorActor])

    @BeanProperty var echoedUserDao: EchoedUserDao = null

    def receive = {
        case ("id", id: String) => {
            logger.debug("Creating EchoedUserService using id {}", id)

            Option(echoedUserDao.findById(id)) match {
                case Some(echoedUser) =>
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(
                            new EchoedUserServiceActor(echoedUser, echoedUserDao)).start)
                    logger.debug("Created EchoedUserService with id {}", id)
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
                            new EchoedUserServiceActor(echoedUser, echoedUserDao, facebookService)).start)
                case None =>
                    logger.debug("Creating EchoedUser with {}", facebookUser)
                    val echoedUser = new EchoedUser(facebookUser)
                    echoedUserDao.insertOrUpdate(echoedUser)
                    facebookService.assignEchoedUser(echoedUser)
                    self.channel ! new EchoedUserServiceActorClient(Actor.actorOf(
                            new EchoedUserServiceActor(echoedUser, echoedUserDao, facebookService)).start)

            }
        }
    }
}
