package com.echoed.chamber.dao

import com.echoed.chamber.domain.shopify.ShopifyPartner


trait ShopifyPartnerDao {

    def findByShopifyId(shopifyId: String): ShopifyPartner

    def findByPartnerId(partnerId: String): ShopifyPartner

    def insertOrUpdate(shopifyUser: ShopifyPartner): Int

}
