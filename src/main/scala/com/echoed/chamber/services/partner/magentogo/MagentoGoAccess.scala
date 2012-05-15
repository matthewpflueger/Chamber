package com.echoed.chamber.services.partner.magentogo

import akka.dispatch.Future
import com.echoed.chamber.domain.magentogo.MagentoGoCredentials


trait MagentoGoAccess {

    def validate(credentials: MagentoGoCredentials): Future[ValidateResponse]

    def fetchOrder(credentials: MagentoGoCredentials, order: Long): Future[FetchOrderResponse]
}
