package com.echoed.chamber.dao

import com.echoed.chamber.domain.magentogo.MagentoGoPartner


trait MagentoGoPartnerDao {

    def findByStoreUrl(storeUrl: String): MagentoGoPartner

    def findByPartnerId(partnerId: String): MagentoGoPartner

    def insert(magentoGoPartner: MagentoGoPartner): Int

}
