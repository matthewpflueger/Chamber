package com.echoed.chamber.domain.partner.shopify


import com.shopify.api.resources.Order


case class ShopifyOrderFull(
        orderId: String,
        orderNumber: String,
        customerId: String,
        shopifyUser: ShopifyPartner,
        lineItems: List[ShopifyLineItem]) {
    
    def this(
            o: Order,
            su: ShopifyPartner,
            lineItems: List[ShopifyLineItem] ) = this(
        o.getId.toString,
        o.getOrderNumber.toString,
        if (o.getCustomer == null) "Guest" else o.getCustomer.getId.toString,
        su,
        lineItems)
    
}
