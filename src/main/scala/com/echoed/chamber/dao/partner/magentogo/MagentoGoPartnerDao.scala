package com.echoed.chamber.dao.partner.magentogo

import com.echoed.chamber.domain.partner.magentogo.MagentoGoPartner


trait MagentoGoPartnerDao {

    def findByStoreUrl(storeUrl: String): MagentoGoPartner

    def findByPartnerId(partnerId: String): MagentoGoPartner

    def insert(magentoGoPartner: MagentoGoPartner): Int

}
