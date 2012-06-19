package com.echoed.chamber.domain.views

import java.util.Date
import com.echoed.chamber.domain.Image


case class EchoViewPersonal(
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
       partnerSettingsMinClicks: Int,
       partnerSettingsMinPercentage: Float,
       image: Image) {

    
    def this(echoView: EchoView ) = this(
            echoView.echoId,
            echoView.echoBoughtOn,
            echoView.echoProductName,
            echoView.echoCategory,
            echoView.echoBrand,
            echoView.echoPrice,
            echoView.echoLandingPageUrl,
            echoView.echoTotalClicks,
            echoView.echoCredit,
            echoView.echoCreditWindowEndsAt,
            echoView.partnerId,
            echoView.partnerName,
            echoView.partnerSettingsMinClicks,
            echoView.partnerSettingsMinPercentage,
            echoView.image)
}
