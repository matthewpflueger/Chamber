package com.echoed.chamber.domain.partner

import java.util.{Calendar, Date}
import com.echoed.util.{ScalaObjectMapper, UUID}
import com.echoed.util.DateUtils._
import com.echoed.chamber.domain.DomainObject
import org.squeryl.annotations.Transient
import java.util


case class PartnerSettings(
        id: String,
        updatedOn: Long,
        createdOn: Long,
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
        couponCode: String,
        couponDescription: String,
        couponExpiresOn: Long,
        activeOn: Long,
        storyPrompts: String,
        customization: String,
        moderateAll: Boolean) extends DomainObject {

    require(closetPercentage >= 0, "Closet percentage is less than 0")
    require(echoedMaxPercentage >= 0, "Echoed max percentage is less than 0")
    require(echoedMatchPercentage >= 0, "Echoed match percentage is less than 0")
    require(minClicks >= 0, "Minimum clicks is less than 0")
    require(creditWindow >= 0, "Credit window is less than 0")

    require(minClicks <= maxClicks, "Minimum clicks is greater than max clicks")
    require(minPercentage >= closetPercentage, "Closet percentage is greater than minimum percentage")
    require(maxPercentage >= minPercentage, "Minimum percentage is greater than maximum percentage")

    require(views != null, "Views is null")

    def this() = this("", 0L, 0L, "", 0f, 0, 0f, 0, 0f, 0f, 0f, 0, "", "", "", "", 0L, 0L, "", null, false)

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
            couponCode: String,
            couponDescription: String,
            couponExpiresOn: Date,
            activeOn: Date) = this(
        UUID(),
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
        couponCode: String,
        couponDescription: String,
        couponExpiresOn: Date,
        activeOn,
        null,
        null,
        false)

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
        "",
        "",
        new Date,
        activeOn)

    def this(partnerId: String, shortName: String) = this(
        partnerId,
        0.01f,
        20,
        0.1f,
        100000,
        0.2f,
        1.0f,
        0.1f,
        168,
        "echo.js.0, echo.js.1",
        "@%s" format shortName,
        new Date)

    @Transient lazy val viewsList = views.split(",").map(_.trim)

    @Transient val isFree = maxPercentage <= 0

    def creditWindowEndsAt = {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR, creditWindow)
        cal.getTime
    }

    def makeStoryPrompts =
            if (storyPrompts == null || storyPrompts.length < 1) StoryPrompts()
            else new ScalaObjectMapper().readValue(storyPrompts, classOf[StoryPrompts])

    def makeCustomizationOptions = {
            var customMap = Map(
                "useGallery" -> false,
                "useRemote" -> true,
                "hideOpener" -> false,
                "remoteVertical" -> "top",
                "remoteHorizontal" -> "left",
                "remoteOrientation" -> "ver",
                "hideOpener" -> true
            )
            if (customization != null && customization.length > 0){
                val map = new ScalaObjectMapper().readValue(customization, classOf[Map[String, Any]])
                for ((key, value) <- map) customMap += (key -> value)
            }
            customMap
    }


    def couponExpiresOnDate: Date = couponExpiresOn
}

object PartnerSettings {
    def createPartnerSettings(partnerId: String) = {
        PartnerSettings(
            id = UUID(),
            updatedOn = new Date,
            createdOn = new Date,
            partnerId = partnerId,
            closetPercentage = 0f,
            minClicks = 0,
            minPercentage = 0f,
            maxClicks = 200,
            maxPercentage = 0f,
            echoedMatchPercentage = 1,
            echoedMaxPercentage = 0.1f,
            creditWindow = 168,
            views = "echo.js.free",
            hashTag = "",
            couponCode = "",
            couponDescription = "",
            couponExpiresOn = new Date,
            activeOn = new Date,
            storyPrompts = null,
            customization = null,
            moderateAll = false)

    }

}
