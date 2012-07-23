package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID
import com.echoed.util.DateUtils._


case class FacebookUser(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUserId: String,
        facebookId: String,
        name: String,
        email: String,
        link: String,
        gender: String,
        timezone: String,
        locale: String,
        accessToken: String) extends DomainObject {

    def this() = this("", 0L, 0L, "", "", "", "", "", "", "","", "")

    def this(
            echoedUserId: String,
            facebookId: String,
            name: String,
            email: String,
            link: String,
            gender: String,
            timezone: String,
            locale: String,
            accessToken: String) = this(
        UUID(),
        new Date,
        new Date,
        echoedUserId,
        facebookId,
        name,
        email,
        link,
        gender,
        timezone,
        locale,
        accessToken)

}


