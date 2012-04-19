package com.echoed.chamber.services.partner

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import scala.reflect.BeanProperty
import com.echoed.chamber.domain.{PartnerUser, PartnerSettings, Partner}


class PartnerServiceManagerActorClient extends PartnerServiceManager with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    def registerPartner(partner: Partner, partnerSettings: PartnerSettings, partnerUser: PartnerUser) =
            (actorRef ? RegisterPartner(partner, partnerSettings, partnerUser)).mapTo[RegisterPartnerResponse]
    
    def updatePartnerSettings(partnerSettings: PartnerSettings) =
            (actorRef ? UpdatePartnerSettings(partnerSettings)).mapTo[UpdatePartnerSettingsResponse]

    def locatePartnerService(partnerId: String) =
            (actorRef ? Locate(partnerId)).mapTo[LocateResponse]

    def locatePartnerByEchoId(echoId: String) =
            (actorRef ? LocateByEchoId(echoId)).mapTo[LocateByEchoIdResponse]
}
