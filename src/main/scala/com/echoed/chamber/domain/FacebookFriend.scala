package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID


case class FacebookFriend(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        facebookUserId: String,
        facebookId: String,
        name: String) {

    def this(facebookUserId: String, facebookId: String, name: String) = this(
        UUID(),
        new Date,
        new Date,
        facebookUserId,
        facebookId,
        name)
}

