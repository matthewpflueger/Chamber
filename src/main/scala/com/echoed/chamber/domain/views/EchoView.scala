package com.echoed.chamber.domain.views

import java.util.Date
import com.echoed.chamber.domain.Image


case class EchoView(
        echoId: String,
        echoBoughtOn: Date,
        echoProductName: String,
        echoCategory: String,
        echoBrand: String,
        echoPrice: Float,
        echoLandingPageUrl: String,
        echoTotalClicks: Int,
        echoCredit: Float,
        echoCreditWindowEndsAt: Date,
        partnerId: String,
        partnerName: String,
        partnerHandle: String,
        partnerSettingsId: String,
        partnerSettingsMinClicks: Int,
        partnerSettingsMinPercentage: Float,
        partnerSettingsMaxPercentage: Float,
        facebookPostId: String,
        facebookPostFacebookId: String,
        twitterStatusId: String,
        twitterStatusTwitterId: String,
        image: Image)

