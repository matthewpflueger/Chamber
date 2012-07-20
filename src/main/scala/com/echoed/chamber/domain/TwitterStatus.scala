package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID


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
        UUID(),
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
