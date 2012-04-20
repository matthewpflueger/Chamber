package com.echoed.chamber.services.partneruser

import akka.dispatch.Future


trait PartnerUserService {

    val id: String

    def activate(password: String): Future[ActivatePartnerUserResponse]

    def getPartnerUser: Future[GetPartnerUserResponse]

    def getCustomerSocialSummary(echoedUserId: String): Future[GetCustomerSocialSummaryResponse]

    def getCustomerSocialActivityByDate(echoedUserId: String): Future[GetCustomerSocialActivityByDateResponse]

    def getPartnerSocialSummary: Future[GetPartnerSocialSummaryResponse]

    def getPartnerSocialActivityByDate: Future[GetPartnerSocialActivityByDateResponse]

    def getProductSocialSummary(productId: String): Future[GetProductSocialSummaryResponse]

    def getProductSocialActivityByDate(productId: String): Future[GetProductSocialActivityByDateResponse]

    def getProducts: Future[GetProductsResponse]

    def getTopProducts: Future[GetTopProductsResponse]

    def getCustomers: Future[GetCustomersResponse]

    def getTopCustomers: Future[GetTopCustomersResponse]

    def getComments: Future[GetCommentsResponse]

    def getCommentsByProductId(productId:String): Future[GetCommentsByProductIdResponse]
    
    def getEchoClickGeoLocation: Future[GetEchoClickGeoLocationResponse]

    def getEchoes: Future[GetEchoesResponse]
    
    //def getEchoes: Future[]

    def logout(partnerUserId: String): Future[LogoutResponse]
}

