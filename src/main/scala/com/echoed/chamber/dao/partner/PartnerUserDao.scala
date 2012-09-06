package com.echoed.chamber.dao.partner

import com.echoed.chamber.domain.partner.PartnerUser


trait PartnerUserDao {

    def insert(partnerUser: PartnerUser): Int

    def findByEmail(email: String): PartnerUser

}
