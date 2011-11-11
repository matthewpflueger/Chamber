package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.domain.EchoedUser


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
    }
}