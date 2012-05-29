package com.echoed.chamber.domain.partner.shopify

case class ShopifyOrder(
        orderId: String,
        orderNumber: String,
        customerId: String,
        lineItem: ShopifyLineItem)

