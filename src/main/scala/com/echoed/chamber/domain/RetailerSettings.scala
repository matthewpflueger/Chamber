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

    def this(
            retailerId: String,
            closetPercentage: Int,
            minClicks: Int,
            minPercentage: Int,
            maxClicks: Int,
            maxPercentage: Int,
            echoedMatchPercentage: Int,
            echoedMaxPercentage: Int,
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


