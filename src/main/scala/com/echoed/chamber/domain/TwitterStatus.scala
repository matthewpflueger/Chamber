package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class TwitterStatus(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        echoId: String,
        echoedUserId: String,
        message: String,
        twitterId: String,
        createdAt: Date,
        text: String,
        source: String,
        postedOn: Date) {

    def this(
            echoId: String,
            echoedUserId: String,
            message: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echoId,
        echoedUserId,
        message,
        null,
        null,
        null,
        null,
        null)


}
