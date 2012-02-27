package com.echoed.chamber.services.partner

import akka.dispatch.Future


trait PartnerService {

    val id: String

    def requestEcho(
            request: String,
            ipAddress: String,
            echoedUserId: Option[String] = None,
            echoClickId: Option[String] = None): Future[RequestEchoResponse]
}

