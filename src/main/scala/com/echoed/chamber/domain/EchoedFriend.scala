package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class EchoedFriend(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        fromEchoedUserId: String,
        toEchoedUserId: String,
        name: String,
        screenName: String,
        facebookUserId: String,
        twitterUserId: String) {

    def this(
            fromEchoedUserId: String,
            toEchoedUserId: String,
            name: String,
            screenName: String,
            facebookUserId: String,
            twitterUserId: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        fromEchoedUserId,
        toEchoedUserId,
        name,
        screenName,
        facebookUserId,
        twitterUserId)

    def this(fromEchoedUser: EchoedUser, toEchoedUser: EchoedUser) = this(
        fromEchoedUser.id,
        toEchoedUser.id,
        toEchoedUser.name,
        toEchoedUser.screenName,
        toEchoedUser.facebookUserId,
        toEchoedUser.twitterUserId)

}
