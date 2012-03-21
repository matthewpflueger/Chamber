package com.echoed.chamber.dao

import com.echoed.chamber.domain.shopify.ShopifyUser

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/16/12
 * Time: 12:51 PM
 * To change this template use File | Settings | File Templates.
 */

trait ShopifyUserDao {

    def findByShopifyId(shopifyId: String): ShopifyUser

    def findByPartnerId(partnerId: String): ShopifyUser

    def insertOrUpdate(shopifyUser: ShopifyUser): Int

}
