package com.echoed.chamber.domain

import scala.reflect.BeanProperty
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
        echoPossibilityId: String) {

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
            echoPossibility.id)

}
