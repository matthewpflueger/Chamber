package com.echoed.chamber.services.partner

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class PartnerServiceActorClient(val actorRef: ActorRef) extends PartnerService with ActorClient {

    val id = actorRef.toString

    private implicit val timeout = Timeout(20 seconds)

    def getPartner =
        (actorRef ? GetPartner()).mapTo[GetPartnerResponse]

    def getEcho(
            echoId: String) =
        (actorRef ? GetEcho(echoId)).mapTo[GetEchoResponse]
    
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
    

    def recordEchoStep(
            echoId: String,
            step: String,
            echoedUserId: Option[String],
            echoClickId: Option[String]) =
        (actorRef ? RecordEchoStep(echoId, step, echoedUserId, echoClickId)).mapTo[RecordEchoStepResponse]

    override def toString = id
}
