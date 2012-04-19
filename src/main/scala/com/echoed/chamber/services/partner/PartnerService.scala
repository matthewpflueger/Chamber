package com.echoed.chamber.services.partner

import akka.dispatch.Future


trait PartnerService {

    val id: String
    
    def requestEcho(
            partnerId: String,
            request: String,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None,
            view: Option[String] = None): Future[RequestEchoResponse]

    def recordEchoStep(
            echoId: String,
            step: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None): Future[RecordEchoStepResponse]

}

