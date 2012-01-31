package com.echoed.chamber.domain

import java.util.{UUID, Date}


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
        crawledOn: Date) {

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
        UUID.randomUUID.toString,
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
        null)

}



