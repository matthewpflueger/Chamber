package com.echoed.chamber.dao

import com.echoed.chamber.domain.FacebookUser


trait FacebookUserDao {

    def findById(id: String): FacebookUser

    def findByEmail(email: String): FacebookUser

    def insertOrUpdate(facebookUser: FacebookUser): Int

    def updateEchoedUser(facebookUser: FacebookUser): Int

    def deleteByEmail(email: String): Int
}
