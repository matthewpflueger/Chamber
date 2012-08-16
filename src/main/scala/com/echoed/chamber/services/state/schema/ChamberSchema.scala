package com.echoed.chamber.services.state.schema

import org.squeryl.Schema
import com.echoed.chamber.domain

private[state] object ChamberSchema extends Schema {

    val adminUsers = table[domain.AdminUser]
    val echoedUsers = table[domain.EchoedUser]
    val facebookUsers = table[domain.FacebookUser]
    val twitterUsers = table[domain.TwitterUser]

    val stories = table[domain.Story]
    val chapters = table[domain.Chapter]
    val chapterImages = table[domain.ChapterImage]
    val comments = table[domain.Comment]

    val notifications = table[Notification]

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

