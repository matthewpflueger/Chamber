package com.echoed.chamber.services.state.schema

import org.squeryl.Schema
import com.echoed.chamber.domain
import domain.Topic


private[state] object ChamberSchema extends Schema {

    val adminUsers = table[domain.AdminUser]
    val echoedUsers = table[domain.EchoedUser]
    val facebookUsers = table[domain.FacebookUser]
    val twitterUsers = table[domain.TwitterUser]

    val images = table[Image]

    val stories = table[domain.Story]
    val chapters = table[domain.Chapter]
    val chapterImages = table[domain.ChapterImage]
    val comments = table[domain.Comment]
    val links = table[domain.Link]
    val moderations = table[domain.Moderation]

    val notifications = table[Notification]
    val echoedUserSettings = table[EchoedUserSettings]
    val schedules = table[Schedule]

    val echoes = table[Echo]

    val partners = table[domain.partner.Partner]
    val partnerSettings = table[domain.partner.PartnerSettings]
    val partnerUsers = table[domain.partner.PartnerUser]

    val votes = table[domain.Vote]

    val followers = table[Follower]

    var topics = table[Topic]

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

    //    val twitterFollowers = table[TwitterFollower]
    //    val twitterStatuses = table[TwitterStatus]


}

