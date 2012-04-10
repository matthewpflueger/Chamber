package com.echoed.chamber.services.partner

import akka.dispatch.Future
import com.echoed.chamber.domain.shopify.ShopifyOrderFull


trait PartnerService {

    val id: String
    
    def getPartner: Future[GetPartnerResponse]
    
    def getPartnerSettings: Future[GetPartnerSettingsResponse]
    
    def requestShopifyEcho(
            order: ShopifyOrderFull,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String,
            echoedUserId: Option[String] = None, 
            echoClickId: Option[String] = None): Future[RequestShopifyEchoResponse]

    def requestEcho(
            request: String,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None): Future[RequestEchoResponse]

    def recordEchoStep(
            echoId: String,
            step: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None): Future[RecordEchoStepResponse]

}

