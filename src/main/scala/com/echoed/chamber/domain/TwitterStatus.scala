package com.echoed.chamber.domain

import com.codahale.jerkson.JsonSnakeCase
import java.util.Date

@JsonSnakeCase
case class TwitterStatus(
      id:String,
      twitterId:String,
      twitterUserId:String,
      text:String,
      createdAt:Date,
      source:String) {

  def this(twitterId:String,twitterUserId:String,text:String, createdAt:Date, source:String) = this(null,twitterId,twitterUserId,text,createdAt,source)

}