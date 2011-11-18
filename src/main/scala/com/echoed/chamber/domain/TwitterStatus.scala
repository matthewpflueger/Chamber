package com.echoed.chamber.domain

import com.codahale.jerkson.JsonSnakeCase
import java.util.Date
import twitter4j.Status

@JsonSnakeCase
case class TwitterStatus(
    id: String,
    twitterId: String,
    twitterUserId: String,
    text: String,
    createdAt: Date,
    source: String,
    var echoId: String,
    var echoedUserId: String
    ) {

    def this(twitterId: String, twitterUserId: String, text: String, createdAt: Date, source: String,echoId: String,  echoedUserId: String) = this (null, twitterId, twitterUserId, text, createdAt, source,echoId,echoedUserId)

    def this(status: Status) = this(
        null,
        status.getId.toString,
        status.getUser.getId.toString,
        status.getText,
        status.getCreatedAt,
        status.getSource,
        null,
        null)

}