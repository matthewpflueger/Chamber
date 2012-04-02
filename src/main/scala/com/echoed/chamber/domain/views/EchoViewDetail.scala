package com.echoed.chamber.domain.views

import com.echoed.chamber.domain.Image


case class EchoViewDetail(
        echoId: String,
        echoProductName: String,
        echoCategory: String,
        echoBrand: String,
        echoLandingPageUrl: String,
        echoedUserId: String,
        echoedUserName: String,
        retailerId: String,
        retailerName: String,
        image: Image)

