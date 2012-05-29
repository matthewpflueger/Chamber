package com.echoed.chamber.services.partner

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import scala.reflect.BeanProperty
import com.echoed.chamber.domain.partner.{PartnerUser, PartnerSettings, Partner}


class PartnerServiceManagerActorClient extends PartnerServiceManager with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    def registerPartner(partner: Partner, partnerSettings: PartnerSettings, partnerUser: PartnerUser) =
        (actorRef ? RegisterPartner(partner, partnerSettings, partnerUser)).mapTo[RegisterPartnerResponse]
    
    def locatePartnerService(partnerId: String) =
        (actorRef ? Locate(partnerId)).mapTo[LocateResponse]

    def locatePartnerByDomain(domain: String) =
        (actorRef ? LocateByDomain(domain)).mapTo[LocateByDomainResponse]

    def locatePartnerByEchoId(echoId: String) =
        (actorRef ? LocateByEchoId(echoId)).mapTo[LocateByEchoIdResponse]

    def getView(partnerId: String) =
        (actorRef ? GetView(partnerId)).mapTo[GetViewResponse]

    def recordEchoStep(
            echoId: String,
            step: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None) =
        (actorRef ? RecordEchoStep(echoId, step, echoedUserId, echoClickId)).mapTo[RecordEchoStepResponse]

    def requestEcho(
            partnerId: String,
            request: String,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None,
            view: Option[String] = None) =
        (actorRef ? RequestEcho(partnerId, request, browserId, ipAddress, userAgent, referrerUrl, echoedUserId, echoClickId, view)).mapTo[RequestEchoResponse]

}
