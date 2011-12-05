package com.echoed.chamber.dao

import com.echoed.chamber.domain.TwitterFollower

import java.util.{List => JList}

trait TwitterFollowerDao {

    def findByTwitterUserId(twitterUserId: String): JList[TwitterFollower]

    def insert(twitterFollower: TwitterFollower): Int

    def deleteByTwitterUserId(twitterUserId: String): Int

}
