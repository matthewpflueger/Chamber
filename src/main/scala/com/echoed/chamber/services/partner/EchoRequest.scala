package com.echoed.chamber.services.partner

import java.util.Date

case class EchoRequest(
        orderId: String,
        customerId: String,
        boughtOn: Date,
        items: List[EchoItem])

