package com.echoed.chamber.dao.partner.shopify

import com.echoed.chamber.domain.partner.shopify.ShopifyPartner


trait ShopifyPartnerDao {

    def findByShopifyId(shopifyId: String): ShopifyPartner

    def findByPartnerId(partnerId: String): ShopifyPartner

    def insert(shopifyPartner: ShopifyPartner): Int

    def update(shopifyPartner: ShopifyPartner): Int

}
