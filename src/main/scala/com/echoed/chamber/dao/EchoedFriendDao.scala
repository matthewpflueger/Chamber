package com.echoed.chamber.dao


import com.echoed.chamber.domain.EchoedFriend

import java.util.{List => JList}

trait EchoedFriendDao {

    def insertOrUpdate(echoedFriend: EchoedFriend): Int

    def findByEchoedUserId(echoedUserId: String): JList[EchoedFriend]

    def deleteByEchoedUserId(echoedUserId: String): Int
}
