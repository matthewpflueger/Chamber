package com.echoed.chamber.domain.views

import java.util.{Date}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/7/12
 * Time: 12:44 PM
 * To change this template use File | Settings | File Templates.
 */

case class RetailerEchoCredit(
    echoId: String,
    orderId: String,
    productName: String, 
    productId: String,
    price: Float,
    totalClicks: Int,
    endDate: Date
){}
