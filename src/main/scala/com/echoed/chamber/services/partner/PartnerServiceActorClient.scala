package com.echoed.chamber.services.partner

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import scala.reflect.BeanProperty
import com.echoed.chamber.domain.{RetailerUser, RetailerSettings, Retailer}
import com.echoed.chamber.domain.shopify.ShopifyOrderFull


class PartnerServiceActorClient(val actorRef: ActorRef) extends PartnerService with ActorClient {

    val id = actorRef.id
    
    def getPartner =
        (actorRef ? GetPartner()).mapTo[GetPartnerResponse]

    def getPartnerSettings =
        (actorRef ? GetPartnerSettings()).mapTo[GetPartnerSettingsResponse]

    def requestEcho(
            request: String,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None,
            view: Option[String] = None) =
        (actorRef ? RequestEcho(request, browserId, ipAddress, userAgent, referrerUrl, echoedUserId, echoClickId, view)).mapTo[RequestEchoResponse]
    
    def requestShopifyEcho(
            shopifyOrder: ShopifyOrderFull,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String,
            echoedUserId: Option[String] = None, 
            echoClickId: Option[String] = None) =
        (actorRef ? RequestShopifyEcho(shopifyOrder, browserId, ipAddress, userAgent, referrerUrl, echoedUserId, echoClickId)).mapTo[RequestShopifyEchoResponse]


    def recordEchoStep(
            echoId: String,
            step: String,
            echoedUserId: Option[String],
            echoClickId: Option[String]) =
        (actorRef ? RecordEchoStep(echoId, step, echoedUserId, echoClickId)).mapTo[RecordEchoStepResponse]

    override def toString = id
}
