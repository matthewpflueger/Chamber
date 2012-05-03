package com.echoed.chamber.domain.shopify
import com.shopify.api.resources.Product


case class ShopifyProduct(id: String,
                     title: String, 
                     category: String,
                     description: String,
                     imageSrc: String,
                     handle: String) {

    
    def this(product:Product) = this(
            product.getId.toString,
            product.getTitle.toString,
            product.getProductType.toString,
            product.getBodyHtml.toString,
            if(product.getImages.size > 0) {
                product.getImages.get(0).getSrc
            } else {
                ""
            },
            product.getHandle)
}
