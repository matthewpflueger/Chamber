package com.echoed.chamber.services.partner.magentogo

import akka.actor.ActorRef
import com.echoed.chamber.services.partner.PartnerServiceActorClient

class MagentoGoPartnerServiceActorClient(actorRef: ActorRef)
        extends PartnerServiceActorClient(actorRef)
        with MagentoGoPartnerService

