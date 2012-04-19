package com.echoed.chamber.domain

import java.util.{Calendar, UUID, Date}


case class PartnerSettings(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        partnerId: String,
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
            partnerId: String,
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
        partnerId,
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

object PartnerSettings {
    def createFuturePartnerSettings(partnerId: String) = {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR,2038)
        cal.set(Calendar.MONTH,0)
        cal.set(Calendar.DAY_OF_MONTH,0)
        cal.set(Calendar.HOUR,0)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)

        PartnerSettings(UUID.randomUUID.toString,
                            new Date,
                            new Date,
                            partnerId,
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
