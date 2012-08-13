package com.echoed.chamber.domain

import org.squeryl.Schema

object ChamberSchema extends Schema {

    val adminUsers = table[AdminUser]
    val echoedUsers = table[EchoedUser]
    val facebookUsers = table[FacebookUser]
    val twitterUsers = table[TwitterUser]

    val stories = table[Story]
    val chapters = table[Chapter]
    val chapterImages = table[ChapterImage]
    val comments = table[Comment]

//    val echoClicks = table[EchoClick]
//    val echoedFriend = table[EchoedFriend]

//    val echoMetrics = table[EchoMetrics]
//    val eventLog = table[EventLog]
//    val facebookComments = table[FacebookComment]
//    val facebookFriends = table[FacebookFriend]
//    val facebookLikes = table[FacebookLike]
//    val facebookPosts = table[FacebookPost]
//    val facebookTestUsers = table[FacebookTestUser]
//    val geoLocations = table[GeoLocation]
//    val images = table[Image]

//    val twitterFollowers = table[TwitterFollower]
//    val twitterStatuses = table[TwitterStatus]


}

