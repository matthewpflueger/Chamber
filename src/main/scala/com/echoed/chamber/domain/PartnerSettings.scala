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
        views: String,
        hashTag: String,
        activeOn: Date) {

    require(closetPercentage >= 0, "Closet percentage is less than 0")
    require(echoedMaxPercentage >= 0, "Echoed max percentage is less than 0")
    require(echoedMatchPercentage >= 0, "Echoed match percentage is less than 0")
    require(minClicks >= 0, "Minimum clicks is less than 0")
    require(creditWindow >= 0, "Credit window is less than 0")

    require(minClicks <= maxClicks, "Minimum clicks is greater than max clicks")
    require(minPercentage >= closetPercentage, "Closet percentage is greater than minimum percentage")
    require(maxPercentage >= minPercentage, "Minimum percentage is greater than maximum percentage")

    require(views != null && views.length > 0, "Views is null or empty")


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
            views: String,
            hashTag: String,
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
        views,
        hashTag,
        activeOn)


    lazy val viewsList = views.split(",").map(_.trim)


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
                            "echo.js.0,echo.js.1,echo.js.2",
                            "",
                            cal.getTime)

    }
    
}
