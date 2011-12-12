package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class EchoedUser(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        email: String,
        screenName: String,
        facebookUserId: String,
        twitterUserId: String) {

    def this(
            name: String,
            email: String,
            screenName: String,
            facebookUserId: String,
            twitterUserId: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        name,
        email,
        null,
        facebookUserId,
        twitterUserId)

    def this(id: String, name: String, email: String) = this(
        id,
        null,
        null,
        name,
        email,
        null,
        null,
        null)

    def this(facebookUser: FacebookUser) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        facebookUser.name,
        facebookUser.email,
        null,
        facebookUser.id,
        null)

    def this(twitterUser: TwitterUser) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        twitterUser.name,
        null,
        twitterUser.screenName,
        null,
        twitterUser.id)
}
