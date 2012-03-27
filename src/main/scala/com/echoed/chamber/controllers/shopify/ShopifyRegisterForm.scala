package com.echoed.chamber.controllers.shopify

import com.echoed.chamber.domain.{RetailerSettings, RetailerUser, Retailer}
import java.util.{UUID, Date}
import org.hibernate.validator.constraints.{NotBlank, Email, URL, Range}
import org.springframework.format.annotation.NumberFormat.Style
import javax.validation.constraints._
import org.springframework.format.annotation.{DateTimeFormat, NumberFormat}
import org.springframework.format.annotation.DateTimeFormat.ISO

case class ShopifyRegisterForm(
                           var partnerId: String = null,
                           var exhibitPercentage: Float = 0.01f,
                           var minClicks: Int = 20,
                           var minPercentage: Float = 0.1f,
                           var maxClicks: Int= 100000,
                           var maxPercentage: Float = 0.2f,
                           var echoedMatchPercentage: Float = 1.0f,
                           var echoedMaxPercentage: Float = 0.1f,
                           var creditWindow: Int = 168,
                           var activeOn: Date = new Date()) {

    def this(partner: Option[String]) = this(partnerId = partner.orNull)

    def this() = {
        this(None)
    }

    def updatePartnerSettings[T](f: (RetailerSettings) => T): T = {
        val partnerSettings = new RetailerSettings(
            partnerId,
            exhibitPercentage,
            minClicks,
            minPercentage,
            maxClicks,
            maxPercentage,
            echoedMatchPercentage,
            echoedMaxPercentage,
            creditWindow,
            activeOn)

        f(partnerSettings)
    }

    @NotBlank
    def getPartnerId = partnerId
    def setPartnerId(partnerId: String) { this.partnerId = partnerId }

    @NumberFormat(style=Style.PERCENT)
    def getExhibitPercentage = exhibitPercentage
    def setExhibitPercentage(exhibitPercentage: Float) { this.exhibitPercentage = exhibitPercentage }

    @Min(1)
    def getMinClicks = minClicks
    def setMinClicks(minClicks: Int) { this.minClicks = minClicks }

    @NumberFormat(style=Style.PERCENT)
    def getMinPercentage = minPercentage
    def setMinPercentage(minPercentage: Float) { this.minPercentage = minPercentage }

    @Min(2)
    def getMaxClicks = maxClicks
    def setMaxClicks(maxClicks: Int) { this.maxClicks = maxClicks }

    @NumberFormat(style=Style.PERCENT)
    def getMaxPercentage = maxPercentage
    def setMaxPercentage(maxPercentage: Float) { this.maxPercentage = maxPercentage }

    @NumberFormat(style=Style.PERCENT)
    def getEchoedMatchPercentage = echoedMatchPercentage
    def setEchoedMatchPercentage(echoedMatchPercentage: Float) { this.echoedMatchPercentage = echoedMatchPercentage }

    @NumberFormat(style=Style.PERCENT)
    def getEchoedMaxPercentage = echoedMaxPercentage
    def setEchoedMaxPercentage(echoedMaxPercentage: Float) { this.echoedMaxPercentage = echoedMaxPercentage }

    @Min(1)
    def getCreditWindow = creditWindow
    def setCreditWindow(creditWindow: Int) { this.creditWindow = creditWindow }

    @DateTimeFormat(iso=ISO.DATE_TIME)
    def getActiveOn = activeOn
    def setActiveOn(activeOn: Date) { this.activeOn = activeOn }

}
