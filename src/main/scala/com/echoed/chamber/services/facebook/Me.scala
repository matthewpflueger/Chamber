package com.echoed.chamber.services.facebook

import com.echoed.chamber.domain.FacebookUser


case class Me(
        id: String,
        name: String,
        first_name: String,
        last_name: String,
        link: String,
        gender: String,
        email: String,
        timezone: String,
        locale: String,
        verified: String,
        updated_time: String) {

    def createFacebookUser(accessToken: String) = new FacebookUser(
        null,
        id,
        name,
        email,
        link,
        gender,
        timezone,
        locale,
        accessToken
    )
}
