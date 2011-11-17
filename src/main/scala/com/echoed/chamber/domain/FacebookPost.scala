package com.echoed.chamber.domain

import java.util.Date


case class FacebookPost(
        id: String,
        message: String,
        picture: String,
        link: String,
        facebookUserId: String,
        echoedUserId: String,
        echoId: String,
        var postedOn: Date,
        var createdOn: Date = new Date,
        var objectId: String)

