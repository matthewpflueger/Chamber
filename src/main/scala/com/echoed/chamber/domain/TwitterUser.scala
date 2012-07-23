package com.echoed.chamber.domain


import java.util.Date
import com.echoed.util.UUID
import com.echoed.util.DateUtils._


case class TwitterUser (
        id: String,
        createdOn: Long,
        updatedOn: Long,
        echoedUserId: String,
        twitterId: String,
        screenName: String,
        name: String,
        profileImageUrl: String,
        location: String,
        timezone: String,
        accessToken: String,
        accessTokenSecret: String) extends DomainObject {

    def this() = this("", 0L, 0L, "", "", "", "", "", "", "","", "")

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
        accessTokenSecret)

}



