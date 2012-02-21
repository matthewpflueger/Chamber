package com.echoed.chamber.dao

import com.echoed.chamber.domain.RetailerUser


trait RetailerUserDao {

    def updatePassword(partnerUser: RetailerUser): Int

    def findById(id: String): RetailerUser

    def insert(retailerUser: RetailerUser): Int

    def deleteById(id: String): Int

    def findByEmail(email: String): RetailerUser

    def deleteByEmail(email: String): Int

}
