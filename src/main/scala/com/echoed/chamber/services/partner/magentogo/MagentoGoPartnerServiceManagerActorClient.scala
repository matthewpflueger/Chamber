package com.echoed.chamber.services.partner.magentogo

import com.echoed.chamber.services.partner.PartnerServiceManagerActorClient
import com.echoed.chamber.services.ActorClient
import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain.magentogo.MagentoGoPartner

class MagentoGoPartnerServiceManagerActorClient
        extends MagentoGoPartnerServiceManager
        with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    def registerPartner(partner: MagentoGoPartner) =
        (actorRef ? RegisterMagentoGoPartner(partner)).mapTo[RegisterMagentoGoPartnerResponse]
}
