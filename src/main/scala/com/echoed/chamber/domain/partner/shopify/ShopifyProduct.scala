package com.echoed.chamber.domain.partner.shopify

import com.echoed.chamber.services.partner.shopify.product

case class ShopifyProduct(
        id: String,
        title: String,
        category: String,
        description: String,
        imageSrc: String,
        handle: String) {

    
    def this(product: product) = this(
            product.id.toString,
            product.title,
            product.productType.toString,
            Option(product.bodyHtml).map(_.toString).getOrElse(""),
            if(product.images.length > 0) {
                product.images(0).src
            } else {
                ""
            },
            product.handle)
}
