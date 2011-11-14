package com.echoed.chamber.dao

import com.echoed.chamber.domain.EchoedUser


trait EchoedUserDao {

    def findById(id: String): EchoedUser

    def insertOrUpdate(echoedUser: EchoedUser): Int

    def findByFacebookUserId(facebookId: String): EchoedUser

    def deleteByEmail(email: String): Int

}
