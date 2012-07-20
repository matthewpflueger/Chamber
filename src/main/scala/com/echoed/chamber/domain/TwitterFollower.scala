package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID


case class TwitterFollower(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        twitterUserId:String,
        twitterId: String,
        name: String) {

    def this(
            twitterUserId: String,
            twitterId: String,
            name: String) = this(
        UUID(),
        new Date,
        new Date,
        twitterUserId,
        twitterId,
        name)


}
