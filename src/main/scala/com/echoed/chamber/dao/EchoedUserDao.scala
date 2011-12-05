package com.echoed.chamber.dao

import com.echoed.chamber.domain.EchoedUser


trait EchoedUserDao {

    def findById(id: String): EchoedUser

    def insert(echoedUser: EchoedUser): Int

    def findByFacebookUserId(facebookId: String): EchoedUser

    def findByTwitterUserId(twitterId: String): EchoedUser

    def deleteByEmail(email: String): Int

    def deleteByScreenName(screenName: String): Int

}
