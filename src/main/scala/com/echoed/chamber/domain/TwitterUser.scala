package com.echoed.chamber.domain


import java.util.Date
import com.echoed.util.UUID


case class TwitterUser (
        id: String,
        createdOn: Date,
        updatedOn: Date,
        echoedUserId: String,
        twitterId: String,
        screenName: String,
        name: String,
        profileImageUrl: String,
        location: String,
        timezone: String,
        accessToken: String,
        accessTokenSecret: String) {


    def this(
            twitterId: String,
            screenName: String,
            name: String,
            profileImageUrl: String,
            location: String,
            timezone: String,
            accessToken: String,
            accessTokenSecret: String) = this(
        UUID(),
        new Date,
        new Date,
        null,
        twitterId,
        screenName,
        name,
        profileImageUrl,
        location,
        timezone,
        accessToken,
        accessTokenSecret
    )

}


