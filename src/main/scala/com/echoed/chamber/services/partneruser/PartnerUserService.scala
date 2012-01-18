package com.echoed.chamber.services.partneruser

import akka.dispatch.Future


trait PartnerUserService {

    val id: String

    def getPartnerUser: Future[GetPartnerUserResponse]

    def getRetailerSocialSummary: Future[GetRetailerSocialSummaryResponse]

    def getProductSocialSummary(productId: String): Future[GetProductSocialSummaryResponse]

    def getTopProducts: Future[GetTopProductsResponse]

    def logout(partnerUserId: String): Future[LogoutResponse]
}

