package com.echoed.chamber.services.partneruser

import akka.actor.Actor
import reflect.BeanProperty
import org.slf4j.LoggerFactory

import com.echoed.chamber.dao.partner.PartnerUserDao
import com.echoed.chamber.dao.views.PartnerViewDao

import scalaz._
import Scalaz._


class PartnerUserServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[PartnerUserServiceCreatorActor])

    @BeanProperty var partnerUserDao: PartnerUserDao = _
    @BeanProperty var partnerViewDao: PartnerViewDao = _

    def receive = {
        case msg @ CreatePartnerUserService(email) =>
            logger.debug("Loading PartnerUser for {}", email)
            Option(partnerUserDao.findByEmail(email)).cata(
                pu => self.channel ! CreatePartnerUserServiceResponse(msg, Right(new PartnerUserServiceActorClient(
                    Actor.actorOf(new PartnerUserServiceActor(pu, partnerUserDao, partnerViewDao)).start))),
                self.channel ! CreatePartnerUserServiceResponse(msg, Left(new PartnerUserException(
                    "No user with email %s" format email))))
    }
}

