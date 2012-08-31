package com.echoed.chamber.services.partner.shopify

import com.echoed.chamber.services.{MessageResponse => MR}
import com.echoed.chamber.domain.partner.{PartnerUser, Partner}
import com.echoed.chamber.domain.partner.shopify.{ShopifyOrderFull, ShopifyPartner}
import com.echoed.chamber.services.partner.{PartnerAlreadyExists, PartnerException => PE, PartnerMessage => PM}
import akka.actor.ActorRef

sealed trait ShopifyPartnerMessage extends PM

sealed case class ShopifyPartnerException(_message: String = "", _cause: Throwable = null) extends PE(_message, _cause)

import com.echoed.chamber.services.partner.shopify.{ShopifyPartnerMessage => SPM}
import com.echoed.chamber.services.partner.shopify.{ShopifyPartnerException => SPE}

import com.echoed.chamber.services.partner.shopify.{order => SO, product => SP}

case class RegisterShopifyPartnerEnvelope(
        shopifyPartner: ShopifyPartner,
        partner: Partner,
        partnerUser: PartnerUser,
        shopifyPartnerService: Option[ActorRef] = None)

case class ShopifyPartnerAlreadyExists(
        envelope: RegisterShopifyPartnerEnvelope,
        __message: String = "Shopify partner already exists",
        __cause: Throwable = null,
        __code: Option[String] = Some("shopify.alreadyexists.partner"),
        __args: Option[Array[AnyRef]] = None) extends PartnerAlreadyExists(
            envelope.shopifyPartnerService.get,
            __message,
            __cause,
            __code,
            __args)

case class RegisterShopifyPartner(shop: String, signature: String, t: String, timeStamp: String) extends SPM
case class RegisterShopifyPartnerResponse(message: RegisterShopifyPartner, value: Either[PE, RegisterShopifyPartnerEnvelope])
        extends SPM
        with MR[RegisterShopifyPartnerEnvelope, RegisterShopifyPartner, PE]

private[shopify] case class Update(shopifyPartner: ShopifyPartner) extends SPM
private[shopify] case class UpdateResponse(message: Update, value: Either[SPE, ShopifyPartner])
        extends SPM
        with MR[ShopifyPartner, Update, SPE]



private[shopify] case class GetShopifyPartner() extends SPM
private[shopify] case class GetShopifyPartnerResponse(message: GetShopifyPartner, value: Either[SPE, ShopifyPartner])
        extends SPM
        with MR[ShopifyPartner, GetShopifyPartner, SPE]

private[shopify] case class GetOrder(orderId: Int) extends SPM
private[shopify] case class GetOrderResponse(message: GetOrder, value: Either[SPE, SO])
        extends SPM
        with MR[SO, GetOrder, SPE]

private[shopify] case class GetOrderFull(orderId: Int) extends SPM
private[shopify] case class GetOrderFullResponse(message: GetOrderFull, value: Either[SPE, ShopifyOrderFull])
        extends SPM
        with MR[ShopifyOrderFull, GetOrderFull, SPE]

private[shopify] case class GetProducts() extends SPM
private[shopify] case class GetProductsResponse(message: GetProducts, value: Either[SPE, List[SP]])
        extends SPM
        with MR[List[SP], GetProducts, SPE]


private[shopify] case class GetShop(password: String) extends SPM
private[shopify] case class GetShopResponse(message: GetShop, value: Either[SPE, ShopifyPartner])
        extends SPM
        with MR[ShopifyPartner, GetShop, SPE]


private[shopify] case class FetchPassword(shop: String, signature: String, t: String, timeStamp: String) extends SPM
private[shopify] case class FetchPasswordResponse(message: FetchPassword, value: Either[SPE, String])
        extends SPM
        with MR[String, FetchPassword, SPE]

private[shopify] case class FetchShop(shop: String, password: String) extends SPM
private[shopify] case class FetchShopResponse(message: FetchShop, value: Either[SPE, ShopifyPartner])
        extends SPM
        with MR[ShopifyPartner, FetchShop, SPE]

private[shopify] case class FetchOrder(shop: String, password: String, orderId: Int) extends SPM
private[shopify] case class FetchOrderResponse(message: FetchOrder, value: Either[SPE, SO])
        extends SPM
        with MR[SO, FetchOrder, SPE]

private[shopify] case class FetchProduct(shop: String, password: String, productId: String) extends SPM
private[shopify] case class FetchProductResponse(message: FetchProduct, value: Either[SPE, SP])
        extends SPM
        with MR[SP, FetchProduct, SPE]

private[shopify] case class FetchProducts(shop: String, password: String) extends SPM
private[shopify] case class FetchProductsResponse(message: FetchProducts, value: Either[SPE, List[SP]])
        extends SPM
        with MR[List[SP], FetchProducts, SPE]

private[shopify] case class FetchShopFromToken(shop: String,  signature: String, t: String,  timeStamp: String)
        extends SPM
private[shopify] case class FetchShopFromTokenResponse(message: FetchShopFromToken, value: Either[SPE, ShopifyPartner])
        extends SPM
        with MR[ShopifyPartner, FetchShopFromToken, SPE]



