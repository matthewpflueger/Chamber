package com.echoed.chamber.controllers

import reflect.BeanProperty
import java.util.Date
import com.echoed.chamber.domain.Echo

@deprecated(message = "This class is part of the old integration method which will be removed asap")
case class EchoPossibilityParameters(
        @BeanProperty var retailerId: String = null,
        @BeanProperty var customerId: String = null,
        @BeanProperty var productId: String = null,
        @BeanProperty var boughtOn: Date = null,
        @BeanProperty var orderId: String = null,
        @BeanProperty var price: Float = 0,
        @BeanProperty var imageUrl: String = null,
        @BeanProperty var echoedUserId: String = null,
        @BeanProperty var echoId: String = null,
        @BeanProperty var landingPageUrl: String = null,
        @BeanProperty var echoPossibilityId: String = null,
        @BeanProperty var productName: String = null,
        @BeanProperty var category: String = null,
        @BeanProperty var brand: String = null,
        @BeanProperty var description: String = null,
        @BeanProperty var echoClickId: String = null,
        @BeanProperty var browserId: String = null,
        @BeanProperty var ipAddress: String = null,
        @BeanProperty var userAgent: String = null,
        @BeanProperty var referrerUrl: String = null) {

    def this() = {
        this(null)
    }


    @deprecated
    def createLoginEchoPossibility = createEchoPossibilityWithStep("login")
    @deprecated
    def createButtonEchoPossibility = createEchoPossibilityWithStep("button")
    @deprecated
    def createConfirmEchoPossibility = createEchoPossibilityWithStep("confirm")
    @deprecated
    def createFacebookEchoPossibility = createEchoPossibilityWithStep("facebook")
    @deprecated
    def createTwitterEchoPossibility = createEchoPossibilityWithStep("twitter")

    private def createEchoPossibilityWithStep(step: String) = Echo.make(
            retailerId = retailerId,
            customerId = customerId,
            productId = productId,
            boughtOn = boughtOn,
            step = step,
            orderId = orderId,
            price = price,
            imageUrl = imageUrl,
            landingPageUrl = landingPageUrl,
            productName = productName,
            category = category,
            brand = brand,
            description = description,
            browserId = browserId,
            ipAddress = ipAddress,
            userAgent = userAgent,
            referrerUrl = referrerUrl,
            echoClickId = echoClickId)

}
