package com.echoed.chamber.services.partner.bigcommerce

import akka.actor.ActorRef
import com.echoed.chamber.services.partner.PartnerServiceActorClient

class BigCommercePartnerServiceActorClient(actorRef: ActorRef)
        extends PartnerServiceActorClient(actorRef)
        with BigCommercePartnerService

