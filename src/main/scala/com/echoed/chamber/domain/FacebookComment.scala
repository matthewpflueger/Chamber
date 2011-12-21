package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class FacebookComment(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        facebookPostId: String,
        facebookUserId: String,
        echoedUserId: String,
        facebookId: String,
        byFacebookId: String,
        name: String,
        message: String,
        createdAt: Date) {

    def this(
            facebookPostId: String,
            facebookUserId: String,
            echoedUserId: String,
            facebookId: String,
            byFacebookId: String,
            name: String,
            message: String,
            createdAt: Date) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        facebookPostId,
        facebookUserId,
        echoedUserId,
        facebookId,
        byFacebookId,
        name,
        message,
        createdAt)

}



