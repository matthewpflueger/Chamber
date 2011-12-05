package com.echoed.chamber.controllers

import reflect.BeanProperty
import java.util.Date
import org.apache.commons.codec.binary.Base64
import collection.mutable.ArrayBuilder
import java.net.URLEncoder
import com.echoed.chamber.domain.EchoPossibility

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
        @BeanProperty var echoPossibilityId: String = null) {


    def this() = {
        this(null)
    }



    def createLoginEchoPossibility = createEchoPossibilityWithStep("login")
    def createButtonEchoPossibility = createEchoPossibilityWithStep("button")
    def createConfirmEchoPossibility = createEchoPossibilityWithStep("confirm")
    def createFacebookEchoPossibility = createEchoPossibilityWithStep("facebook")
    def createTwitterEchoPossibility = createEchoPossibilityWithStep("twitter")

    private def createEchoPossibilityWithStep(step: String) = new EchoPossibility(
            retailerId,
            customerId,
            productId,
            boughtOn,
            step,
            orderId,
            price,
            imageUrl,
            echoedUserId,
            echoId,
            landingPageUrl)

}
