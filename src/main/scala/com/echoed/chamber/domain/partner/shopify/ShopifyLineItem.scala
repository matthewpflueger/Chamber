package com.echoed.chamber.domain.partner.shopify

import com.echoed.chamber.services.partner.shopify.LineItem


case class ShopifyLineItem(
        productId: String,
        price: String,
        product: ShopifyProduct) {
    
    def this(l: LineItem, p: ShopifyProduct) = this(
            l.productId,
            l.price,
            p)
}
