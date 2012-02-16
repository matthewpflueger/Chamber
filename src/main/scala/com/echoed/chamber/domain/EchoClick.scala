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
        referrerUrl: String,
        ipAddress: String,
        clickedOn: Date,
        forwardedFor: String,
        echoPossibilityId: String) {


    def this(
            echoId: String,
            echoedUserId: String,
            referrerUrl: String,
            ipAddress: String,
            forwardedFor: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echoId,
        null,
        null,
        echoedUserId,
        referrerUrl,
        ipAddress,
        new Date,
        forwardedFor,
        null    )

}
