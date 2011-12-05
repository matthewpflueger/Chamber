package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class Echo(
        id: String,
        createdOn: Date,
        updatedOn: Date,
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
        fee: Float) {

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
            totalClicks: Int,
            credit: Float,
            fee: Float) = this(
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
        totalClicks,
        credit,
        fee)

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
        0)


    def this(
            echoPossibility: EchoPossibility,
            retailerSettingsId: String,
            credit: Float,
            fee: Float) = this(
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
        retailerSettingsId,
        0,
        credit,
        fee)

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
        0)


}


