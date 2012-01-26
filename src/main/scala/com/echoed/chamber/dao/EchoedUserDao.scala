package com.echoed.chamber.dao

import com.echoed.chamber.domain.EchoedUser


trait EchoedUserDao {

    def findById(id: String): EchoedUser

    def insert(echoedUser: EchoedUser): Int

    def findByFacebookUserId(facebookUserId: String): EchoedUser

    def findByFacebookId(facebookId: String): EchoedUser

    def findByTwitterId(twitterId: String): EchoedUser

    def findByTwitterUserId(twitterUserId: String): EchoedUser

    def findByEmail(email: String): EchoedUser

    def deleteByEmail(email: String): Int

    def deleteByScreenName(screenName: String): Int

    def update(echoedUser: EchoedUser): Int

    def unlinkTwitter(echoedUser: EchoedUser): Int
}
