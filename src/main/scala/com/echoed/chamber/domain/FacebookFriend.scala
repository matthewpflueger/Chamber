package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class FacebookFriend(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        facebookUserId: String,
        facebookId: String,
        name: String) {

    def this(facebookUserId: String, facebookId: String, name: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        facebookUserId,
        facebookId,
        name)
}

