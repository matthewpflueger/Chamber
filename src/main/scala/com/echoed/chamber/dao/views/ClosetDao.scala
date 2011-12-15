package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.Closet


trait ClosetDao {

    def findByEchoedUserId(echoedUserId: String): Closet

    def totalCreditByEchoedUserId(echoedUserId: String): Float

}
