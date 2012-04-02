package com.echoed.chamber.domain.views


import java.util.Date
import com.echoed.chamber.domain.Image


case class EchoViewPublic(
        echoId: String,
        echoBoughtOn: Date,
        echoProductName: String,
        echoCategory: String,
        echoBrand: String,
        echoLandingPageUrl: String,
        retailerName: String,
        image: Image){
    
    def this(echoView: EchoView) = this(
            echoView.echoId,
            echoView.echoBoughtOn,
            echoView.echoProductName,
            echoView.echoCategory,
            echoView.echoBrand,
            echoView.echoLandingPageUrl,
            echoView.retailerName,
            echoView.image)
}

