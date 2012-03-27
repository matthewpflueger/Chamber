package com.echoed.chamber.domain

import java.util.{Calendar, UUID, Date}


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
        creditWindow: Int,
        activeOn: Date) {

    require(closetPercentage >= 0)
    require(echoedMaxPercentage >= 0)
    require(echoedMatchPercentage >= 0)
    require(minClicks >= 0)
    require(creditWindow >= 0)

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
            creditWindow: Int,
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
        creditWindow,
        activeOn)


    def creditWindowEndsAt = {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR, creditWindow)
        cal.getTime
    }
}

object RetailerSettings { 
    def createFutureRetailerSettings(retailerId: String) = {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR,2038)
        cal.set(Calendar.MONTH,0)
        cal.set(Calendar.DAY_OF_MONTH,0)
        cal.set(Calendar.HOUR,0)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)
        RetailerSettings(UUID.randomUUID.toString,
                            new Date,
                            new Date,
                            retailerId,
                            0.001f,
                            1,
                            0.0011f,
                            1,
                            0.0012f,
                            1,
                            0.1f,
                            168,
                            cal.getTime)
    }
    
}
