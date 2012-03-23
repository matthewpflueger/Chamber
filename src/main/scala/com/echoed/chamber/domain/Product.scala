package com.echoed.chamber.domain

import java.util.Date


case class Product(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        productId: String,
        price: Float,
        landingPageUrl: String,
        productName: String,
        category: String,
        brand: String,
        description: String)



