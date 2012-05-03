package com.echoed.chamber.dao

import com.echoed.chamber.domain.NetworkSolutionsPartner


trait NetworkSolutionsPartnerDao {

    def findByUserKey(userKey: String): NetworkSolutionsPartner

    def findByPartnerId(partnerId: String): NetworkSolutionsPartner

    def findByDomain(domain: String): NetworkSolutionsPartner

    def findByEmail(email: String): NetworkSolutionsPartner

    def insert(networkSolutionsPartner: NetworkSolutionsPartner): Int

    def update(networkSolutionsPartner: NetworkSolutionsPartner): Int

}
