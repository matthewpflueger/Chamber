package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID


case class FacebookLike(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        facebookPostId: String,
        facebookUserId: String,
        echoedUserId: String,
        facebookId: String,
        name: String) {

    def this(
            facebookPostId: String,
            facebookUserId: String,
            echoedUserId: String,
            facebookId: String,
            name: String) = this(
        UUID(),
        new Date,
        new Date,
        facebookPostId,
        facebookUserId,
        echoedUserId,
        facebookId,
        name)

}



