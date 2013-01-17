package com.echoed.chamber.domain.views
import com.echoed.chamber.domain.{TwitterUser,  TwitterStatus}

case class TwitterStatusData(
    id:             String,
    twitterStatus:  TwitterStatus,
    twitterUser:    TwitterUser) {

}