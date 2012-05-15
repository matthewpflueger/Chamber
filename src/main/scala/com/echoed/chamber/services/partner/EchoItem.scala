package com.echoed.chamber.services.partner

case class EchoItem(
        productId: String,
        productName: String,
        category: String,
        brand: String,
        price: Float,
        imageUrl: String,
        landingPageUrl: String,
        description: String) {

    lazy val isValid =
        Option(productId).filter(_.length > 0).isDefined &&
        Option(productName).filter(_.length > 0).isDefined &&
        Option(price).filter(_ > 0).isDefined &&
        Option(imageUrl).filter(_.length > 0).isDefined &&
        Option(landingPageUrl).filter(_.length > 0).isDefined &&
        Option(description).filter(_.length > 0).isDefined

}

