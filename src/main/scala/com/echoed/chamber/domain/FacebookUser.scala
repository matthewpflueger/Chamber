package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class FacebookUser(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        echoedUserId: String,
        facebookId: String,
        name: String,
        email: String,
        link: String,
        gender: String,
        timezone: String,
        locale: String,
        accessToken: String) {

    def this(
            echoedUserId: String,
            facebookId: String,
            name: String,
            email: String,
            link: String,
            gender: String,
            timezone: String,
            locale: String,
            accessToken: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echoedUserId,
        facebookId,
        name,
        email,
        link,
        gender,
        timezone,
        locale,
        accessToken)

}

