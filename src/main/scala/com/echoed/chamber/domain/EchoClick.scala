package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class EchoClick(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        echoId: String,
        facebookPostId: String,
        twitterStatusId: String,
        echoedUserId: String,
        browserId: String,
        ipAddress: String,
        userAgent: String,
        referrerUrl: String,
        filtered: Boolean) {

    def this(
            echoId: String,
            echoedUserId: String,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echoId,
        null,
        null,
        echoedUserId,
        browserId,
        ipAddress,
        userAgent,
        referrerUrl,
        false)


}
