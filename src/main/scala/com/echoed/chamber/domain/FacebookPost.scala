package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID


case class FacebookPost(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        message: String,
        caption: String,
        picture: String,
        link: String,
        facebookUserId: String,
        echoedUserId: String,
        echoId: String,
        postedOn: Date,
        facebookId: String,
        crawledStatus: String,
        crawledOn: Date,
        retries: Int) {

    def this(
            name: String, 
            message: String,
            caption: String,
            picture: String,
            link: String,
            facebookUserId: String,
            echoedUserId: String,
            echoId: String,
            postedOn: Date = null,
            facebookId: String = null) = this(
        UUID(),
        new Date,
        new Date,
        name,
        message,
        caption,
        picture,
        link,
        facebookUserId,
        echoedUserId,
        echoId,
        postedOn,
        facebookId,
        null,
        null,
        0)

}



