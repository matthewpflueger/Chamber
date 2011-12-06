package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class RetailerSettings(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        retailerId: String,
        closetPercentage: Float,
        minClicks: Int,
        minPercentage: Float,
        maxClicks: Int,
        maxPercentage: Float,
        echoedMatchPercentage: Float,
        echoedMaxPercentage: Float,
        activeOn: Date) {

    require(closetPercentage >= 0)
    require(echoedMaxPercentage >= 0)
    require(echoedMatchPercentage >= 0)
    require(minClicks >= 0)

    require(minClicks <= maxClicks)
    require(minPercentage >= closetPercentage)
    require(maxPercentage >= minPercentage)


    def this(
            retailerId: String,
            closetPercentage: Float,
            minClicks: Int,
            minPercentage: Float,
            maxClicks: Int,
            maxPercentage: Float,
            echoedMatchPercentage: Float,
            echoedMaxPercentage: Float,
            activeOn: Date) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        retailerId,
        closetPercentage,
        minClicks,
        minPercentage,
        maxClicks,
        maxPercentage,
        echoedMatchPercentage,
        echoedMaxPercentage,
        activeOn)

}


