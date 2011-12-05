package com.echoed.chamber.dao

import com.echoed.chamber.domain.FacebookPost

import java.util.{List => JList}

trait FacebookPostDao {

    def findByEchoId(echoId: String): FacebookPost

    def findByEchoedUserId(echoedUserId: String): JList[FacebookPost]

    def insert(facebookPost: FacebookPost): Int

    def updatePostedOn(facebookPost: FacebookPost): Int

    def deleteByEchoedUserId(echoedUserId: String): Int

}
