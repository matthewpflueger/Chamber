package com.echoed.chamber.dao

import com.echoed.chamber.domain.bigcommerce.BigCommercePartner


trait BigCommercePartnerDao {

    def findByStoreUrl(storeUrl: String): BigCommercePartner

    def findByPartnerId(partnerId: String): BigCommercePartner

    def insert(bigCommercePartner: BigCommercePartner): Int

}
