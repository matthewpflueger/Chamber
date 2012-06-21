package com.echoed.chamber.dao.partner

import com.echoed.chamber.domain.partner.Partner
import org.apache.ibatis.annotations.Param


trait PartnerDao {

    def findById(id: String): Partner

    def findByDomain(domain: String): Partner

    def insert(partner: Partner): Int

    def deleteById(id: String): Int

    def deleteByName(name: String): Int

    def update(partner: Partner): Int

    def updateHandleAndCategory(
        @Param("partnerId") partnerId: String,
        @Param("partnerHandle") partnerHandle: String,
        @Param("partnerCategory") partnerCategory: String): Int

}
