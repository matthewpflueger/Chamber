package com.echoed.chamber.dao.partner.bigcommerce

import com.echoed.chamber.domain.partner.bigcommerce.BigCommercePartner


trait BigCommercePartnerDao {

    def findByStoreUrl(storeUrl: String): BigCommercePartner

    def findByPartnerId(partnerId: String): BigCommercePartner

    def insert(bigCommercePartner: BigCommercePartner): Int

}
