package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._


case class EchoPossibilityView(
        echoPossibility: EchoPossibility,
        retailer: Retailer,
        retailerSettings: RetailerSettings,
        echoedUser: EchoedUser,
        echo: Echo) {

    def this(echoPossibility: EchoPossibility, retailer: Retailer, retailerSettings: RetailerSettings) = this(
        echoPossibility,
        retailer,
        retailerSettings,
        null,
        null)
}

