package com.echoed.chamber.domain


import reflect.BeanProperty
import com.codahale.jerkson.JsonSnakeCase

@JsonSnakeCase
case class FacebookUser(
        id: String,
        username: String,
        firstName: String,
        lastName: String,
        link: String,
        gender: String,
        email: String,
        timezone: String,
        locale: String) {

    @BeanProperty var accessToken: String = null
    @BeanProperty var echoedUserId: String = null

    def getId = id
}