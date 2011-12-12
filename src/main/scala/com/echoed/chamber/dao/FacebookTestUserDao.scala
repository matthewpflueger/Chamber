package com.echoed.chamber.dao

import com.echoed.chamber.domain.FacebookTestUser

import java.util.{List => JList}

trait FacebookTestUserDao {

    def insertOrUpdate(facebookTestUser: FacebookTestUser): Int

    def findById(id: String): FacebookTestUser

    def findByEmail(email: String): FacebookTestUser

    def deleteByEmail(id: String): Int

    def selectAll: JList[FacebookTestUser]

    def selectFirst(number: Int): JList[FacebookTestUser]
}
