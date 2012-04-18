package com.echoed.chamber.domain.shopify

import java.util.{UUID, Date}
import com.shopify.api.resources.Shop


case class ShopifyPartner(
                          id: String,
                          updatedOn: Date,
                          createdOn: Date,
                          shopifyId: String,
                          domain: String,
                          name: String,
                          zip: String,
                          shopOwner: String,
                          email: String,
                          phone: String,
                          country: String,
                          city: String,
                          shopifyDomain: String,
                          password: String,
                          partnerId: String) {

    def this(shop: Shop) = this(
        UUID.randomUUID().toString,
        new Date,
        new Date,
        shop.getId.toString,
        shop.getDomain,
        shop.getName,
        shop.getZip,
        shop.getShopOwner,
        shop.getEmail,
        shop.getPhone,
        shop.getCountry,
        shop.getCity,
        shop.getMyshopifyDomain,
        "",
        UUID.randomUUID().toString
    )

}
