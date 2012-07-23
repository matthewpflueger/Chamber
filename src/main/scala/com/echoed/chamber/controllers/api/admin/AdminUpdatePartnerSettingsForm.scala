package com.echoed.chamber.controllers.api.admin

import com.echoed.chamber.domain.partner.PartnerSettings
import java.util.Date
import org.hibernate.validator.constraints.NotBlank
import org.springframework.format.annotation.NumberFormat.Style
import javax.validation.constraints._
import org.springframework.format.annotation.{DateTimeFormat, NumberFormat}

case class AdminUpdatePartnerSettingsForm(
                            var partnerId: String = null,
                            var closetPercentage: Float = 0.01f,
                            var minClicks: Int = 0,
                            var minPercentage: Float = 0.00f,
                            var maxClicks: Int = 200,
                            var maxPercentage: Float = 0.10f,
                            var echoedMatchPercentage: Float = 1f,
                            var echoedMaxPercentage: Float = 0.10f,
                            var creditWindow: Int = 1,
                            var views: String = null,
                            var hashTag: String = null,
                            var couponCode: String = null,
                            var couponDescription: String = null,
                            var couponExpiresOn: Date = new Date(),
                            var activeOn: Date = new Date()) {

    def this() = {
        this(null)
    }


    def createPartnerSettings[T](f: (PartnerSettings) => T): T = {
        val partnerSettings = new PartnerSettings(
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
            couponCode,
            couponDescription,
            couponExpiresOn,
            new Date())
        f(partnerSettings)
    }

    @NotBlank
    def getPartnerId() = partnerId
    def setPartnerId(partnerId: String) = { this.partnerId = partnerId }

    @NumberFormat(style=Style.PERCENT)
    def getClosetPercentage() = closetPercentage
    def setClosetPercentage(closetPercentage: Float) = { this.closetPercentage = closetPercentage}

    @Min(0)
    def getMinClicks() = minClicks
    def setMinClicks(minClicks: Int) = { this.minClicks = minClicks}

    @NumberFormat(style=Style.PERCENT)
    def getMinPercentage() = minPercentage
    def setMinPercentage(minPercentage: Float) = { this.minPercentage = minPercentage }

    @Min(1)
    def getMaxClicks() = maxClicks
    def setMaxClicks(maxClicks: Int) = { this.maxClicks = maxClicks }

    @NumberFormat(style=Style.PERCENT)
    def getMaxPercentage() = maxPercentage
    def setMaxPercentage(maxPercentage: Float) = { this.maxPercentage = maxPercentage }

    @NumberFormat(style=Style.PERCENT)
    def getEchoedMatchPercentage() = echoedMatchPercentage
    def setEchoedMatchPercentage(echoedMatchPercentage: Float) = { this.echoedMatchPercentage = echoedMatchPercentage }

    @NumberFormat(style=Style.PERCENT)
    def getEchoedMaxPercentage() = echoedMaxPercentage
    def setEchoedMaxPercentage(echoedMaxPercentage: Float) = { this.echoedMaxPercentage = echoedMaxPercentage }

    @Min(24)
    def getCreditWindow() = creditWindow
    def setCreditWindow(creditWindow: Int) = { this.creditWindow = creditWindow }

    @NotBlank
    def getViews() = views
    def setViews(views: String) = { this.views = views }

    def getHashTag() = hashTag
    def setHashTag(hashTag: String) = { this.hashTag = hashTag}

    def getCouponCode() = couponCode
    def setCouponCode(couponCode: String) = { this.couponCode = couponCode }

    def getCouponDescription() = couponDescription
    def setCouponDescription(couponDescription: String) = { this.couponDescription = couponDescription}

    @DateTimeFormat(pattern = "MM-dd-YYYY")
    def getCouponExpiresOn() = couponExpiresOn
    def setCouponExpiresOn(couponExpiresOn: Date) = { this.couponExpiresOn = couponExpiresOn }

    @DateTimeFormat(pattern = "MM-dd-YYYY")
    def getActiveOn() = activeOn
    def setActiveOn(activeOn: Date) = { this.activeOn = activeOn }


}
