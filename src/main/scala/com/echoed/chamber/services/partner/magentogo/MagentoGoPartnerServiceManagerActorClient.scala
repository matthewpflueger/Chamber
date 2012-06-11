package com.echoed.chamber.services.partner.magentogo

import com.echoed.chamber.services.ActorClient
import akka.actor.ActorRef
import akka.pattern.ask
import reflect.BeanProperty
import com.echoed.chamber.domain.partner.magentogo.MagentoGoPartner
import akka.util.Timeout
import akka.util.duration._

class MagentoGoPartnerServiceManagerActorClient
        extends MagentoGoPartnerServiceManager
        with ActorClient {

    @BeanProperty var actorRef: ActorRef = _
    @BeanProperty var timeoutInSeconds = 20

    def registerPartner(partner: MagentoGoPartner) =
        (actorRef.ask(RegisterMagentoGoPartner(partner))(Timeout(timeoutInSeconds seconds))).mapTo[RegisterMagentoGoPartnerResponse]
}
