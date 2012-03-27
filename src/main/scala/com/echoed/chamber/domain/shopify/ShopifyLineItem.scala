package com.echoed.chamber.domain.shopify

import com.shopify.api.resources.{Order, LineItem}

case class ShopifyLineItem(
        productId: String,
        price: String,
        product: ShopifyProduct) {
    
    def this(l: LineItem, p: ShopifyProduct) = this(
                                        l.getProductId.toString,
                                        l.getPrice,
                                        p)
}
