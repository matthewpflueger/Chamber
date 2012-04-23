package com.echoed.chamber.domain.views

import java.util.Date

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 4/19/12
 * Time: 9:29 AM
 * To change this template use File | Settings | File Templates.
 */

case class PartnerEchoView(
        id: String,
        createdOn: Date,
        orderId: String,
        productId: String,
        productName: String,
        price: Float,
        totalClicks: Int,
        credit: Float,
        creditWindowEndsAt : Date) {


}
