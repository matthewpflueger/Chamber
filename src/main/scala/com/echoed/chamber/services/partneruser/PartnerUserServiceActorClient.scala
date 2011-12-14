package com.echoed.chamber.services.partneruser

import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.Closet


class PartnerUserServiceActorClient(partnerUserServiceActor: ActorRef) extends PartnerUserService {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerUserServiceActorClient])

    def getPartnerUser = (partnerUserServiceActor ? GetPartnerUser()).mapTo[GetPartnerUserResponse]

}
