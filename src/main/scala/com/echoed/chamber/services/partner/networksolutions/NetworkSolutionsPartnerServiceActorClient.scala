package com.echoed.chamber.services.partner.networksolutions

import akka.actor.ActorRef
import com.echoed.chamber.services.partner.PartnerServiceActorClient

class NetworkSolutionsPartnerServiceActorClient(actorRef: ActorRef)
        extends PartnerServiceActorClient(actorRef)
        with NetworkSolutionsPartnerService {


}
