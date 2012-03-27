package com.echoed.chamber.domain.shopify
import com.shopify.api.resources.Product

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/25/12
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */

case class ShopifyProduct(id: String,
                     title: String, 
                     category: String,
                     description: String,
                     imageSrc: String) {

    
    def this(product:Product) = this(
                                    product.getId.toString,
                                    product.getTitle.toString,
                                    product.getProductType.toString,
                                    product.getBodyHtml.toString,
                                    if(product.getImages.size > 0) {
                                        product.getImages.get(0).getSrc
                                    } else {
                                        ""
                                    })
}
