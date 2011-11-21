package com.echoed.chamber.domain

import java.util.Date


case class Echo(
        id: String,
        retailerId: String,
        customerId: String,
        productId: String,
        boughtOn: Date,
        orderId: String,
        price: String,
        imageUrl: String,
        echoedUserId: String,
        var facebookPostId: String,
        var twitterStatusId: String,
        echoPossibilityId: String) {

    def this(id: String, boughtOn: Date, price: String, imageUrl: String) = this(
            id,
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
            null)

    def this(echoPossibility: EchoPossibility) = this(
            null,
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
            echoPossibility.id)

}
