package com.echoed.chamber.services.partneruser

import akka.dispatch.Future
import com.echoed.util.FutureHelper

import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.Closet


trait PartnerUserService {

    def getPartnerUser: Future[GetPartnerUserResponse]
    
    def getRetailerSocialSummary: Future[GetRetailerSocialSummaryResponse]
    
    def getProductSocialSummary(productId: String): Future[GetProductSocialSummaryResponse]
    
    def getTopProducts: Future[GetTopProductsResponse]

}

