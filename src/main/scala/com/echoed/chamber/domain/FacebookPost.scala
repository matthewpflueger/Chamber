package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class FacebookPost(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        message: String,
        picture: String,
        link: String,
        facebookUserId: String,
        echoedUserId: String,
        echoId: String,
        postedOn: Date,
        facebookId: String) {

    def this(
            message: String,
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
        message,
        picture,
        link,
        facebookUserId,
        echoedUserId,
        echoId,
        postedOn,
        facebookId)

}



