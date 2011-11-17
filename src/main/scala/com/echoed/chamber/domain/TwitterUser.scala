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

  @BeanProperty var accessToken: String = _
  @BeanProperty var accessTokenSecret: String = _
  @BeanProperty var echoedUserId: String = _

}

