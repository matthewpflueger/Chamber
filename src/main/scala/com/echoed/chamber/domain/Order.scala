package com.echoed.chamber.domain

import java.util.Date


case class Order(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        customerId: String,
        boughtOn: Date,
        orderId: String)


