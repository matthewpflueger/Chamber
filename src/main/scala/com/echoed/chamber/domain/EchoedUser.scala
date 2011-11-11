package com.echoed.chamber.domain

import reflect.BeanProperty
import java.util.UUID


case class EchoedUser(
        id: String, // = UUID.randomUUID().toString,
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        facebookUserId: String,
        twitterUserId: String) {

}