package com.echoed.chamber.services.partner

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import scala.reflect.BeanProperty
import com.echoed.chamber.domain.{RetailerUser, RetailerSettings, Retailer}


class PartnerServiceManagerActorClient extends PartnerServiceManager with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    def registerPartner(partner: Retailer, partnerSettings: RetailerSettings, partnerUser: RetailerUser) =
            (actorRef ? RegisterPartner(partner, partnerSettings, partnerUser)).mapTo[RegisterPartnerResponse]

    def locatePartnerService(partnerId: String) =
            (actorRef ? Locate(partnerId)).mapTo[LocateResponse]

    def locatePartnerByEchoId(echoId: String) =
            (actorRef ? LocateByEchoId(echoId)).mapTo[LocateByEchoIdResponse]
}
