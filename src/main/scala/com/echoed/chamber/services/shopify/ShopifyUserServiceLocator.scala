package com.echoed.chamber.services.shopify

import akka.dispatch.Future

trait ShopifyUserServiceLocator {


    //def create(shop:String,signature:String,t:String, timeStamp: String): CreateResponse
    
    def locate(shop: String, signature: String, t: String, timeStamp: String): Future[LocateByTokenResponse]

    def locateByPartnerId(partnerId: String): Future[LocateByPartnerIdResponse]
    
}
