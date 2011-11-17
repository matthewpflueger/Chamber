package com.echoed.chamber.dao

import com.echoed.chamber.domain.FacebookPost


trait FacebookPostDao {

    def findByEchoId(echoId: String): FacebookPost

    def insert(facebookPost: FacebookPost): Int

    def updatePostedOn(facebookPost: FacebookPost): Int

}
