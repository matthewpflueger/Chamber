package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.Closet
import org.apache.ibatis.annotations.Param


trait ClosetDao {

    def findByEchoedUserId(
        @Param("id") echoedUserId: String,
        @Param("start") start: Int, 
        @Param("limit") limit: Int): Closet

    def totalCreditByEchoedUserId(echoedUserId: String): Float

    def totalClicksByEchoedUserId(echoedUserId: String): Int

}
