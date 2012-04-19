package com.echoed.chamber.dao

import com.echoed.chamber.domain.Partner


trait PartnerDao {

    def findById(id: String): Partner

    def insert(partner: Partner): Int

    def deleteById(id: String): Int

    def deleteByName(name: String): Int
}
