package com.echoed.chamber.dao

import com.echoed.chamber.domain.shopify.ShopifyPartner


trait ShopifyPartnerDao {

    def findByShopifyId(shopifyId: String): ShopifyPartner

    def findByPartnerId(partnerId: String): ShopifyPartner

    def insert(shopifyPartner: ShopifyPartner): Int

    def update(shopifyPartner: ShopifyPartner): Int

}
