package com.echoed.chamber.domain.partner.shopify

import java.util.Date
import com.echoed.chamber.domain.partner.{PartnerUser, Partner}
import com.echoed.util.UUID


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

    def this(
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
            password: String) = this(
        UUID(),
        new Date,
        new Date,
        shopifyId,
        domain,
        name,
        zip,
        shopOwner,
        email,
        phone,
        country,
        city,
        shopifyDomain,
        password,
        null)
}

object ShopifyPartner {
    def createPartner(shopifyPartner: ShopifyPartner) = {
        require(shopifyPartner.partnerId == null, "Partner already created for %s" format shopifyPartner)

        val partner = new Partner(
            name = shopifyPartner.name,
            domain = shopifyPartner.domain,
            phone = shopifyPartner.phone,
            handle = null,
            logo = null,
            category = "Other").copy(cloudPartnerId = "Shopify")

        (partner, shopifyPartner.copy(partnerId = partner.id))
    }

    def createPartnerUser(shopifyPartner: ShopifyPartner) = {
        require(shopifyPartner.partnerId != null, "Partner is null for %s" format this)
        new PartnerUser(shopifyPartner.partnerId, shopifyPartner.shopOwner, shopifyPartner.email)
    }
}
