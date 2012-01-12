package com.echoed.chamber.domain


import java.util.{Date, UUID}


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
        UUID.randomUUID.toString,
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

//    def this(user: User, accessToken: String, accessTokenSecret: String) = this(
//        null,
//        user.getId.toString,
//        user.getScreenName,
//        user.getName,
//        user.getProfileImageURL.toExternalForm,
//        user.getLocation,
//        user.getTimeZone,
//        accessToken,
//        accessTokenSecret)

}


