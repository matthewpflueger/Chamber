package com.echoed.chamber.dao

import com.echoed.chamber.domain.TwitterUser

trait TwitterUserDao {

    def findById(id: String): TwitterUser

    def findByTwitterId(twitterId: String): TwitterUser

    def findByScreenName(screenName: String): TwitterUser

    def insert(twitterUser: TwitterUser): Int

    def updateEchoedUser(twitterUser: TwitterUser): Int

    def deleteById(id: String): Int

    def deleteByScreenName(screenName: String): Int
}
