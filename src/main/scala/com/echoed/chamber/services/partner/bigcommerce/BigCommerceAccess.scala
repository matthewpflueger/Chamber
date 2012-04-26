package com.echoed.chamber.services.partner.bigcommerce

import akka.dispatch.Future
import com.echoed.chamber.domain.bigcommerce.BigCommerceCredentials


trait BigCommerceAccess {

    def validate(credentials: BigCommerceCredentials): Future[ValidateResponse]

    def fetchOrder(credentials: BigCommerceCredentials, order: Long): Future[FetchOrderResponse]
}
