package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._
import partner.{PartnerSettings, Partner}


case class EchoFull(
        id: String,
        echo: Echo, 
        partner: Partner,
        echoedUser: EchoedUser, 
        facebookPost: FacebookPost,
        twitterStatus: TwitterStatus,
        partnerSettings: PartnerSettings) {

    def this(
            echo: Echo,
            partner: Partner,
            echoedUser: EchoedUser,
            facebookPost: FacebookPost,
            twitterStatus: TwitterStatus,
            partnerSettings: PartnerSettings) = this(
        echo.id,
        echo,
        partner,
        echoedUser,
        facebookPost,
        twitterStatus,
        partnerSettings)

    def this(
            echo: Echo,
            echoedUser: EchoedUser,
            partnerSettings: PartnerSettings) = this(echo, null, echoedUser, null, null, partnerSettings)

}

