package com.echoed.chamber.domain

import java.util.{UUID, Date}

case class FacebookTestUser(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        echoedUserId: String,
        facebookUserId: String,
        facebookId: String,
        name: String,
        email: String,
        password: String,
        loginUrl: String,
        accessToken: String) {

    def this(
            echoedUserId: String,
            facebookUserId: String,
            facebookId: String,
            name: String,
            email: String,
            password: String,
            loginUrl: String,
            accessToken: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echoedUserId,
        facebookUserId,
        facebookId,
        name,
        email,
        password,
        loginUrl,
        accessToken)


    def this(
            facebookId: String,
            name: String,
            email: String,
            password: String,
            loginUrl: String,
            accessToken: String) = this(
        null,
        null,
        facebookId,
        name,
        email,
        password,
        loginUrl,
        accessToken)

    def createFacebookUser = new FacebookUser(
        echoedUserId = null,
        facebookId = this.facebookId,
        name = this.name,
        email = this.email,
        link = "link",
        gender = "male",
        timezone = "-5",
        locale = "en_US",
        accessToken = this.accessToken)

}

