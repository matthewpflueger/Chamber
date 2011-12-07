package com.echoed.util

import com.echoed.chamber.domain._
import com.echoed.chamber.controllers.EchoPossibilityParameters
import java.util.{Calendar, Date, UUID}


class DataCreator {

    val on = new Date
    val today = Calendar.getInstance()
    val past = {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, today.get(Calendar.YEAR)-1)
        c
    }
    val future = {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, today.get(Calendar.YEAR)+1)
        c
    }

    val echoedUserId = UUID.randomUUID.toString
    val retailerUserId = UUID.randomUUID.toString
    val twitterUserId = UUID.randomUUID.toString
    val facebookUserId = UUID.randomUUID.toString
    val retailerId = UUID.randomUUID.toString

    val landingPageUrl = "http://www.echoed.com/"
    val echoImageUrl = "http://echoed.com/images/logo.png"
    val twitterUserProfileImageUrl = "http://echoed.com/images/logo.png"

    val retailer = Retailer(
        retailerId,
        on,
        on,
        "Test Retailer")

    val retailerSettings = List(
        RetailerSettings(
            id = UUID.randomUUID.toString,
            updatedOn = on,
            createdOn = on,
            retailerId = retailerId,
            closetPercentage = 0.01f,
            minClicks = 1,
            minPercentage = 0.1f,
            maxClicks = 10,
            maxPercentage = 0.2f,
            echoedMatchPercentage = 1f,
            echoedMaxPercentage = 0.2f,
            activeOn = past.getTime),
        RetailerSettings(
            id = UUID.randomUUID.toString,
            updatedOn = on,
            createdOn = on,
            retailerId = retailerId,
            closetPercentage = 0.01f,
            minClicks = 1,
            minPercentage = 0.1f,
            maxClicks = 10,
            maxPercentage = 0.2f,
            echoedMatchPercentage = 1f,
            echoedMaxPercentage = 0.2f,
            activeOn = future.getTime)
    )

    val twitterId ="47851866"
    val twitterScreenName = "MisterJWU"
    val twitterPassword = "gateway2"

    val twitterUser = TwitterUser(
        twitterUserId,
        on,
        on,
        echoedUserId,
        twitterId,
        twitterScreenName,
        "Test TwitterUser",
        twitterUserProfileImageUrl,
        "location",
        "timezone",
        "accessToken",
        "accessTokenSecret")

    /*
        curl -v 'https://graph.facebook.com/177687295582534/accounts/test-users?access_token=177687295582534|zXC5wmZqodeHhTpUVXThov7zKrA&name=TestUser&permissions=email,publish_stream,offline_access&method=post&installed=true'

        //OLD {"id":"100003128184602","access_token":"AAAChmwwiYUYBAJG7MomgcAy1ZCg0fEuXBSjM45n80FV0CHofT1VLZCeGp805f5qt6odHkKBMUwB9n75GJZCrzmbc3nZCDUZBpuxT4WyXliQZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003128184602&n=R0ZipMc3NCuutvb","email":"testuser_jasdmrk_testuser\u0040tfbnw.net","password":"970285973"}
        {"id":"100003177284815","access_token":"AAAChmwwiYUYBAKI2bxTrAgnIgLMok1r8Xel3lgBqu0uqR8RtFaxdzXVEzek7MYNlkIxZB4TXcZCZCZBnzM8auZAWZAZCJLNotEhu1tL24ImxAZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003177284815&n=8L2tMNJBPGMWlAE","email":"testuser_jpmknrv_testuser\u0040tfbnw.net","password":"273385869"}
    */
    val facebookId = "100003177284815"
    val echoedUserEmail = "testuser_jpmknrv_testuser@tfbnw.net"
    val facebookUserPassword = "273385869"
    val facebookUserLoginPageUrl = "https://www.facebook.com/platform/test_account_login.php?user_id=100003177284815&n=8L2tMNJBPGMWlAE"


    val facebookUser = FacebookUser(
        facebookUserId,
        on,
        on,
        echoedUserId,
        facebookId,
        "Test FacebookUser",
        echoedUserEmail,
        "http://www.facebook.com/profile.php?id=%s" format facebookId,
        "male",
        "-5",
        "en_US",
        "AAAChmwwiYUYBAKI2bxTrAgnIgLMok1r8Xel3lgBqu0uqR8RtFaxdzXVEzek7MYNlkIxZB4TXcZCZCZBnzM8auZAWZAZCJLNotEhu1tL24ImxAZDZD")

    val facebookFriends = List(
        new FacebookFriend(
            facebookUserId,
            facebookId,
            "Test FacebookFriend 1"
        ),
        new FacebookFriend(
            facebookUserId,
            facebookId,
            "Test FacebookFriend 2"
        )
    )

    val twitterFollowerId = "twitterFollowerId"
    val twitterFollowerName = "twitterFollowerName"
    val twitterFollowers = List(
        new TwitterFollower(
            twitterUserId,
            twitterFollowerId,
            twitterFollowerName
        ),
        new TwitterFollower(
            twitterUserId,
            twitterFollowerId,
            twitterFollowerName
        )
    )

    val retailerUserPassword = "testpassword"
    val retailerUser = new RetailerUser(
        retailerId,
        "Test RetailerUser",
        "TestRetailerUser@echoed.com"
    ).createPassword(retailerUserPassword)

    val echoedUser = EchoedUser(
        echoedUserId,
        on,
        "Test EchoedUser",
        echoedUserEmail,
        twitterUser.screenName,
        facebookUser.id,
        twitterUser.id)


    val facebookPostId_1 = UUID.randomUUID.toString
    val facebookPostId_2 = UUID.randomUUID.toString
    val twitterStatusId_1 = UUID.randomUUID.toString
    val twitterStatusId_2 = UUID.randomUUID.toString
    val echoId_1 = UUID.randomUUID.toString
    val echoId_2 = UUID.randomUUID.toString

    val orderId_1 = "orderId_1"
    val productId_1 = "productId_1"
    val customerId_1 = "customerId_1"
    val price_1 = 10.00f

    val orderId_2 = "orderId_2"
    val productId_2 = "productId_2"
    val customerId_2 = "customerId_2"
    val price_2 = 20.00f

    val echoPossibilities = List(
        EchoPossibilityParameters(
            retailerId,
            customerId_1,
            productId_1,
            on,
            orderId_1,
            price_1,
            echoImageUrl,
            echoedUserId,
            echoId_1,
            landingPageUrl,
            null).createButtonEchoPossibility,
        EchoPossibilityParameters(
            retailerId,
            customerId_2,
            productId_2,
            on,
            orderId_2,
            price_2,
            echoImageUrl,
            echoedUserId,
            echoId_2,
            landingPageUrl,
            null).createButtonEchoPossibility
    )

    val twitterStatuses = List(
        TwitterStatus(
            twitterStatusId_1,
            on,
            on,
            echoId_1,
            echoedUserId,
            "message",
            "twitterId",
            new Date,
            "text",
            "source",
            new Date
        ),
        TwitterStatus(
            twitterStatusId_2,
            on,
            on,
            echoId_2,
            echoedUserId,
            "message",
            "twitterId",
            new Date,
            "text",
            "source",
            new Date
        )
    )

    val facebookPosts = List(
        FacebookPost(
            facebookPostId_1,
            on,
            on,
            "message",
            "picture",
            "link",
            facebookUserId,
            echoedUserId,
            echoId_1,
            on,
            "facebookId_1"),
        FacebookPost(
            facebookPostId_2,
            on,
            on,
            "message",
            "picture",
            "link",
            facebookUserId,
            echoedUserId,
            echoId_2,
            on,
            "facebookId_2")
    )

    val echoClicks_1 = List(
        EchoClick(
            UUID.randomUUID.toString,
            on,
            on,
            echoId_1,
            facebookPostId_1,
            null,
            echoedUserId,
            "http://facebook.com",
            "127.0.0.1",
            on),
        EchoClick(
            UUID.randomUUID.toString,
            on,
            on,
            echoId_1,
            null,
            twitterStatusId_1,
            echoedUserId,
            "http://twitter.com",
            "127.0.0.1",
            on)
    )

    val echoClicks_2 = List(
        EchoClick(
            UUID.randomUUID.toString,
            on,
            on,
            echoId_2,
            facebookPostId_2,
            null,
            echoedUserId,
            "http://facebook.com",
            "127.0.0.1",
            on),
        EchoClick(
            UUID.randomUUID.toString,
            on,
            on,
            echoId_2,
            null,
            twitterStatusId_2,
            echoedUserId,
            "http://twitter.com",
            "127.0.0.1",
            on)
    )

    val echoes = List(
        Echo(
            echoId_1,
            on,
            on,
            retailerId,
            customerId_1,
            productId_1,
            on,
            orderId_1,
            price_1,
            echoImageUrl,
            echoedUserId,
            facebookPostId_1,
            twitterStatusId_1,
            echoPossibilities(0).id,
            landingPageUrl,
            retailerSettings(0).id,
            0,
            0,
            0),
        Echo(
            echoId_2,
            on,
            on,
            retailerId,
            customerId_2,
            productId_2,
            on,
            orderId_2,
            price_2,
            echoImageUrl,
            echoedUserId,
            facebookPostId_2,
            twitterStatusId_2,
            echoPossibilities(1).id,
            landingPageUrl,
            retailerSettings(1).id,
            0,
            0,
            0)
    )
}
