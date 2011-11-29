package com.echoed.chamber.domain.views

import java.util.Date
import com.echoed.chamber.domain._


case class EchoFull(
        id: String,
        retailer: Retailer,
        customerId: String,
        productId: String,
        boughtOn: Date,
        orderId: String,
        price: String,
        imageUrl: String,
        echoedUser: EchoedUser,
        facebookPost: FacebookPost,
        twitterStatus: TwitterStatus,
        echoPossibilityId: String,
        landingPageUrl: String) {

    def this(
            echo: Echo,
            retailer: Retailer,
            echoedUser: EchoedUser,
            facebookPost: FacebookPost,
            twitterStatus: TwitterStatus) = this(
        echo.id,
        retailer,
        echo.customerId,
        echo.productId,
        echo.boughtOn,
        echo.orderId,
        echo.price,
        echo.imageUrl,
        echoedUser,
        facebookPost,
        twitterStatus,
        echo.echoPossibilityId,
        echo.landingPageUrl)

}

