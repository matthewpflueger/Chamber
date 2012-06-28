package com.echoed.chamber.domain.partner.shopify

import com.echoed.chamber.services.partner.shopify.order


case class ShopifyOrderFull(
        orderId: String,
        orderNumber: String,
        customerId: String,
        shopifyUser: ShopifyPartner,
        lineItems: List[ShopifyLineItem]) {
    
    def this(
            o: order,
            su: ShopifyPartner,
            lineItems: List[ShopifyLineItem] ) = this(
        o.id.toString,
        o.orderNumber,
        if (o.customer == null) "Guest" else o.customer.id.toString,
        su,
        lineItems)
    
}
