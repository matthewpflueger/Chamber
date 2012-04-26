package com.echoed.chamber.services.partner.bigcommerce

import com.echoed.chamber.services.partner.PartnerServiceManagerActorClient
import com.echoed.chamber.services.ActorClient
import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain.bigcommerce.BigCommercePartner

class BigCommercePartnerServiceManagerActorClient
        extends BigCommercePartnerServiceManager
        with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    def registerPartner(partner: BigCommercePartner) =
        (actorRef ? RegisterBigCommercePartner(partner)).mapTo[RegisterBigCommercePartnerResponse]
}
