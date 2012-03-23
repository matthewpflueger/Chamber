package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._


case class EchoPossibilityView(
        echoPossibilities: List[Echo],
        retailer: Retailer,
        retailerSettings: RetailerSettings,
        echoedUser: EchoedUser,
        echo: Echo) {

    def this(echoPossibilities: List[Echo], retailer: Retailer, retailerSettings: RetailerSettings) = this(
        echoPossibilities,
        retailer,
        retailerSettings,
        null,
        null)

    def this(echoPossibility: Echo, retailer: Retailer, retailerSettings: RetailerSettings) = this(
        List(echoPossibility),
        retailer,
        retailerSettings)

    val echoPossibility = echoPossibilities(0)
}

