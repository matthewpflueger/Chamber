package com.echoed.chamber.domain.shopify


import com.shopify.api.resources.{Order, Product}


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/25/12
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */

case class ShopifyOrderFull(
                orderId: String,
                orderNumber: String, 
                customerId: String,
                shopifyUser: ShopifyUser, 
                lineItems: List[ShopifyLineItem]) {
    
    def this(o: Order, su: ShopifyUser, lineItems: List[ShopifyLineItem] ) = this(o.getId.toString, o.getOrderNumber.toString, o.getCustomer.getId.toString, su, lineItems)
    
}
