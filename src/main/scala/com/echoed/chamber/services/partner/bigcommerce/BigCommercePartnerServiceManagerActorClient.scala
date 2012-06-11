package com.echoed.chamber.services.partner.bigcommerce

import com.echoed.chamber.services.ActorClient
import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain.partner.bigcommerce.BigCommercePartner
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

class BigCommercePartnerServiceManagerActorClient
        extends BigCommercePartnerServiceManager
        with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def registerPartner(partner: BigCommercePartner) =
        (actorRef ? RegisterBigCommercePartner(partner)).mapTo[RegisterBigCommercePartnerResponse]
}
