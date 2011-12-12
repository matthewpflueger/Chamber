package com.echoed.chamber.dao

import com.echoed.chamber.domain.{FacebookFriend, TwitterFollower}

import java.util.{List => JList}


trait FacebookFriendDao {

    def findByFacebookUserId(facebookUserId: String): JList[FacebookFriend]

    def insertOrUpdate(facebookFriend: FacebookFriend): Int

    def deleteByFacebookUserId(facebookUserId: String): Int

}
