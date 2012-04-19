package com.echoed.chamber.domain.views

import java.util.Date


case class PartnerEchoCredit(
    echoId: String,
    orderId: String,
    productName: String, 
    productId: String,
    price: Float,
    totalClicks: Int,
    endDate: Date)
