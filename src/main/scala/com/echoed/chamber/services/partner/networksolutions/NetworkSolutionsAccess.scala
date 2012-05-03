package com.echoed.chamber.services.partner.networksolutions

import akka.dispatch.Future


trait NetworkSolutionsAccess {

    def fetchUserKey(successUrl: String, failureUrl: Option[String] = None): Future[FetchUserKeyResponse]

    def fetchUserToken(userKey: String): Future[FetchUserTokenResponse]

    def fetchOrder(userToken: String, orderNumber: Long): Future[FetchOrderResponse]

}
