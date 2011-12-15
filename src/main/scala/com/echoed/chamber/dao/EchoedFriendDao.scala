package com.echoed.chamber.dao


import org.apache.ibatis.annotations.Param
import com.echoed.chamber.domain.EchoedFriend

import java.util.{List => JList}

trait EchoedFriendDao {

    def insertOrUpdate(echoedFriend: EchoedFriend): Int

    def findFriendByEchoedUserId(
        @Param("echoedUserId") echoedUserId: String,
        @Param("echoedFriendId") echoedFriendId: String): EchoedFriend

    def findByEchoedUserId(echoedUserId: String): JList[EchoedFriend]

    def deleteByEchoedUserId(echoedUserId: String): Int
}
