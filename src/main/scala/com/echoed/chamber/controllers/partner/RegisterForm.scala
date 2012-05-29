package com.echoed.chamber.controllers.partner

import com.echoed.chamber.domain.partner.{PartnerSettings, PartnerUser, Partner}
import java.util.{UUID, Date}
import org.hibernate.validator.constraints.{NotBlank, Email, URL}
import org.springframework.format.annotation.NumberFormat.Style
import javax.validation.constraints._
import org.springframework.format.annotation.{DateTimeFormat, NumberFormat}
import org.springframework.format.annotation.DateTimeFormat.ISO

case class RegisterForm(
        var name: String = null,
        var website: String = null,
        var phone: String = null,
        var hashTag: String = null,
        var logo: String = null,
        var exhibitPercentage: Float = 0.01f,
        var minClicks: Int = 20,
        var minPercentage: Float = 0.1f,
        var maxClicks: Int= 100000,
        var maxPercentage: Float = 0.2f,
        var echoedMatchPercentage: Float = 1.0f,
        var echoedMaxPercentage: Float = 0.1f,
        var creditWindow: Int = 168,
        var activeOn: Date = new Date(),
        var userName: String = null,
        var email: String = null,
        var category: String = null) {

    def this() = {
        this(null)
    }


    def createPartner[T](f: (Partner, PartnerSettings, PartnerUser) => T): T = {
        val partner = new Partner(name, website, phone, hashTag, logo, category)
        val partnerSettings = new PartnerSettings(
                partner.id,
                exhibitPercentage,
                minClicks,
                minPercentage,
                maxClicks,
                maxPercentage,
                echoedMatchPercentage,
                echoedMaxPercentage,
                creditWindow,
                "echo.js.0, echo.js.1",
                hashTag,
                activeOn)
        val partnerUser = new PartnerUser(partner.id, name, email).createPassword(UUID.randomUUID().toString)

        f(partner, partnerSettings, partnerUser)
    }


    @NotBlank
    def getCategory() = category
    def setCategory(category: String) { this.category = category }

    @NotBlank
    def getUserName() = userName
    def setUserName(userName: String) { this.userName = userName }

    @Email
    @NotBlank
    def getEmail = email
    def setEmail(email: String) { this.email = email }

    @NotBlank
    def getName = name
    def setName(name: String) { this.name = name }

    @URL(protocol = "http")
    @NotBlank
    def getWebsite() = website
    def setWebsite(website: String) { this.website = website }

    @NotBlank
    def getPhone = phone
    def setPhone(phone: String) { this.phone = phone }

    @Pattern(regexp = "^$|^@.*", message="{regexp.hashTag}")
    def getHashTag = hashTag
    def setHashTag(hashTag: String) { this.hashTag = hashTag }

    @URL(protocol = "http")
    def getLogo = logo
    def setLogo(logo: String) { this.logo = logo }

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
