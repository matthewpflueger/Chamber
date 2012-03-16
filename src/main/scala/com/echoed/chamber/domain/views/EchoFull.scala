package com.echoed.chamber.domain.views

import java.util.Date
import com.echoed.chamber.domain._


case class EchoFull(
        id: String,
        echo: Echo, 
        retailer: Retailer,
        echoedUser: EchoedUser, 
        facebookPost: FacebookPost,
        twitterStatus: TwitterStatus,
        retailerSettings: RetailerSettings) {

    def this(
            echo: Echo,
            retailer: Retailer,
            echoedUser: EchoedUser,
            facebookPost: FacebookPost,
            twitterStatus: TwitterStatus,
            retailerSettings: RetailerSettings) = this(
        echo.id,
        echo,
        retailer,
        echoedUser,
        facebookPost,
        twitterStatus,
        retailerSettings)

    def this(echo: Echo, echoedUser: EchoedUser,retailerSettings: RetailerSettings) = this(echo, null, echoedUser, null, null, retailerSettings)

}

