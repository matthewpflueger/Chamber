package com.echoed.chamber.dao

import com.echoed.chamber.domain.TwitterStatus

trait TwitterStatusDao {

  def selectTwitterStatusWithId(id:String): TwitterStatus

  def insertOrUpdate(twitterStatus:TwitterStatus): Int

}