package com.echoed.chamber.dao

import com.echoed.chamber.domain.TwitterStatus

import java.util.{List => JList}

trait TwitterStatusDao {

    def findById(id: String): TwitterStatus

    def insert(twitterStatus:TwitterStatus): Int

    def findByEchoedUserId(echoedUserId: String): JList[TwitterStatus]

    def updatePostedOn(twitterStatus: TwitterStatus): Int

    def deleteByEchoedUserId(echoedUserId: String): Int

}
