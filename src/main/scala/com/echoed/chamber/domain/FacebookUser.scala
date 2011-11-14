package com.echoed.chamber.domain


import reflect.BeanProperty
import com.codahale.jerkson.JsonSnakeCase

@JsonSnakeCase
case class FacebookUser(
        id: String,
        firstName: String,
        lastName: String,
        link: String,
        gender: String,
        email: String,
        timezone: String,
        locale: String) {

    @BeanProperty var username: String = _
    @BeanProperty var accessToken: String = _
    @BeanProperty var echoedUserId: String = _

    def getId = id
}
