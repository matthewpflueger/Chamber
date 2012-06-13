package com.echoed.chamber.domain.partner.shopify

import com.shopify.api.resources.Product

case class ShopifyProduct(
        id: String,
        title: String,
        category: String,
        description: String,
        imageSrc: String,
        handle: String) {

    
    def this(product:Product) = this(
            product.getId.toString,
            product.getTitle.toString,
            product.getProductType.toString,
            Option(product.getBodyHtml).map(_.toString).getOrElse(""),
            if(product.getImages.size > 0) {
                product.getImages.get(0).getSrc
            } else {
                ""
            },
            product.getHandle)
}