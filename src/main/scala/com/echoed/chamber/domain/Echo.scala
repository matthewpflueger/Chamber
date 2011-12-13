package com.echoed.chamber.domain

import java.util.{UUID, Date}
import java.lang.{Math => JMath}


case class Echo(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        retailerId: String,
        customerId: String,
        productId: String,
        boughtOn: Date,
        orderId: String,
        price: Float,
        imageUrl: String,
        echoedUserId: String,
        facebookPostId: String,
        twitterStatusId: String,
        echoPossibilityId: String,
        landingPageUrl: String,
        retailerSettingsId: String,
        totalClicks: Int,
        credit: Float,
        fee: Float,
        productName: String, 
        category: String, 
        brand: String) {

    def this(
            retailerId: String,
            customerId: String,
            productId: String,
            boughtOn: Date,
            orderId: String,
            price: Float,
            imageUrl: String,
            echoedUserId: String,
            facebookPostId: String,
            twitterStatusId: String,
            echoPossibilityId: String,
            landingPageUrl: String,
            retailerSettingsId: String,
            productName: String, 
            category: String,
            brand: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        retailerId,
        customerId,
        productId,
        boughtOn,
        orderId,
        price,
        imageUrl,
        echoedUserId,
        facebookPostId,
        twitterStatusId,
        echoPossibilityId,
        landingPageUrl,
        retailerSettingsId,
        0,
        0,
        0,
        productName,
        category,
        brand)

    def this(id: String, boughtOn: Date, price: Int, imageUrl: String, landingPageUrl: String) = this(
        id,
        null,
        null,
        null,
        null,
        null,
        boughtOn,
        null,
        price,
        imageUrl,
        null,
        null,
        null,
        null,
        landingPageUrl,
        null,
        0,
        0,
        0,
        null,
        null,
        null)


    def this(
            echoPossibility: EchoPossibility,
            retailerSettings: RetailerSettings) = this(
        echoPossibility.retailerId,
        echoPossibility.customerId,
        echoPossibility.productId,
        echoPossibility.boughtOn,
        echoPossibility.orderId,
        echoPossibility.price,
        echoPossibility.imageUrl,
        echoPossibility.echoedUserId,
        null,
        null,
        echoPossibility.id,
        echoPossibility.landingPageUrl,
        retailerSettings.id,
        echoPossibility.productName,
        echoPossibility.category,
        echoPossibility.brand)

    def this(
            id: String,
            boughtOn: Date,
            price: Float,
            imageUrl: String,
            landingPageUrl: String) = this(
        id,
        null,
        null,
        null,
        null,
        null,
        boughtOn,
        null,
        price,
        imageUrl,
        null,
        null,
        null,
        null,
        landingPageUrl,
        null,
        0,
        0,
        0,
        null,
        null,
        null)


    def echoed(rs: RetailerSettings) = {
        require(retailerSettingsId == rs.id)
        if (credit > 0 || fee > 0) throw new IllegalStateException("Already echoed")
        update(0, rs.closetPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage)
    }

    def clicked(rs: RetailerSettings) = {
        require(retailerSettingsId == rs.id)

        val totalClicks = this.totalClicks+1

        if (totalClicks < 1 || totalClicks < rs.minClicks || totalClicks > rs.maxClicks) {
            this.copy(totalClicks = totalClicks)
        } else if (totalClicks == rs.minClicks) {
            update(totalClicks, rs.minPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage)
        } else if (totalClicks == rs.maxClicks) {
            update(totalClicks, rs.maxPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage)
        } else {
            val percentage = logOfBase(rs.maxClicks, totalClicks - rs.minClicks) * (rs.maxPercentage - rs.minPercentage) + rs.minPercentage
            update(totalClicks, percentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage)
        }
    }

    private def update(
            totalClicks: Int,
            percentage: Float,
            echoedMaxPercentage: Float,
            echoedMatchPercentage: Float) = {

        val credit = price * percentage
        val fee =
            if (echoedMaxPercentage < percentage) {
                price * echoedMaxPercentage
            } else {
                credit * echoedMatchPercentage
            }
        this.copy(totalClicks = totalClicks, credit = credit, fee = fee)
    }

    private def logOfBase(base: Int, number: Int) = (JMath.log(number) / JMath.log(base)).floatValue()
}


