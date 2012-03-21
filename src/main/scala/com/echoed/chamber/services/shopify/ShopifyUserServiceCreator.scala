package com.echoed.chamber.services.shopify

import akka.dispatch.Future

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/16/12
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */

trait ShopifyUserServiceCreator {
    
    def createFromPartnerId(partnerId: String): Future[CreateFromPartnerIdResponse]

    def createFromToken(shop: String,  signature: String, t: String,  timeStamp: String): Future[CreateFromTokenResponse]
    
    def createFromShopDomain(shopDomain: String): Future[CreateFromShopDomainResponse]
    
}
