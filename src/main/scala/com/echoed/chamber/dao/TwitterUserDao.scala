package com.echoed.chamber.dao

import com.echoed.chamber.domain.TwitterUser

trait TwitterUserDao {

  def selectTwitterUserWithId(id: String): TwitterUser

  def insertOrUpdateTwitterUser(twitterUser: TwitterUser): Int


}