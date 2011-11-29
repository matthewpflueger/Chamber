package com.echoed.chamber.domain

import java.util.Date


case class EchoClick(
        id: String,
        echoId: String,
        facebookPostId: String,
        twitterStatusId: String,
        echoedUserId: String,
        referrerUrl: String,
        ipAddress: String,
        clickedOn: Date = new Date) {


    def this(echoId: String, echoedUserId: String, referrerUrl: String, ipAddress: String) =
            this(null, echoId, null, null, echoedUserId, referrerUrl, ipAddress)

}
