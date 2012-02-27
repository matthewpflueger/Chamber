package com.echoed.chamber.services.partner

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import scala.reflect.BeanProperty
import com.echoed.chamber.domain.{RetailerUser, RetailerSettings, Retailer}


class PartnerServiceActorClient(val actorRef: ActorRef) extends PartnerService with ActorClient {

    val id = actorRef.id

    def requestEcho(
            request: String,
            ipAddress: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None) =
        (actorRef ? RequestEcho(request, ipAddress, echoedUserId, echoClickId)).mapTo[RequestEchoResponse]
}
