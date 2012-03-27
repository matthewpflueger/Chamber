package com.echoed.chamber.services.partner

import akka.dispatch.Future
import com.echoed.chamber.domain.shopify.ShopifyOrderFull


trait PartnerService {

    val id: String
    
    def getPartner: Future[GetPartnerResponse]
    
    def requestShopifyEcho(
            order: ShopifyOrderFull,
            ipAddress: String,
            echoedUserId: Option[String] = None, 
            echoClickId: Option[String] = None): Future[RequestShopifyEchoResponse]

    def requestEcho(
            request: String,
            ipAddress: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None): Future[RequestEchoResponse]

    def recordEchoStep(
            echoId: String,
            step: String,
            ipAddress: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None): Future[RecordEchoStepResponse]

}

