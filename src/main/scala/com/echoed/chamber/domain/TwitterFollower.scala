package com.echoed.chamber.domain

import java.util.{UUID, Date}


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
        UUID.randomUUID.toString,
        new Date,
        new Date,
        twitterUserId,
        twitterId,
        name)


}
