package com.echoed.chamber.services.shopify

import akka.dispatch.Future

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/16/12
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */

trait ShopifyAccess {
    
    def fetchPassword(shop: String,  signature: String,  t: String, timeStamp:String): Future[FetchPasswordResponse]
    
    def fetchShop(shop: String, password: String): Future[FetchShopResponse]
    
    def fetchOrder(shop: String, password: String,  orderId: Int): Future[FetchOrderResponse]
    
    def fetchProducts(shop: String, password: String): Future[FetchProductsResponse]
    
}
