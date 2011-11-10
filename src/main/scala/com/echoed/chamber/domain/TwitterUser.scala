package com.echoed.chamber.domain

import reflect.BeanProperty
import com.codahale.jerkson.JsonSnakeCase

@JsonSnakeCase
case class TwitterUser (
        id: String,
        username: String,
        name: String,
        location: String,
        timezone: String
        ){
  @BeanProperty var accessToken: String = null
  @BeanProperty var accessTokenSecret: String = null
  @BeanProperty var echoedUserId: String = null

}

