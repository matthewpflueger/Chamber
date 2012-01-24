package com.echoed.chamber.services.partneruser

import akka.dispatch.Future


trait PartnerUserService {

    val id: String

    def getPartnerUser: Future[GetPartnerUserResponse]

    def getRetailerSocialSummary: Future[GetRetailerSocialSummaryResponse]
    
    def getRetailerSocialActivityByDate: Future[GetRetailerSocialActivityByDateResponse]

    def getProductSocialSummary(productId: String): Future[GetProductSocialSummaryResponse]

    def getProductSocialActivityByDate(productId: String): Future[GetProductSocialActivityByDateResponse]
    
    def getProducts: Future[GetProductsResponse]

    def getTopProducts: Future[GetTopProductsResponse]

    def getCustomers: Future[GetCustomersResponse]

    def getTopCustomers: Future[GetTopCustomersResponse]

    def logout(partnerUserId: String): Future[LogoutResponse]
}

