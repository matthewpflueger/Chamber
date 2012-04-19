package com.echoed.chamber.services.partner.shopify

import com.echoed.chamber.services.{ResponseMessage => RM}
import com.echoed.chamber.domain._
import com.shopify.api.resources.{Order => SO, Product => SP}
import shopify.{ShopifyOrderFull, ShopifyPartner}
import com.echoed.chamber.services.partner.{PartnerAlreadyExists, PartnerService, PartnerException => PE, PartnerMessage => PM}

sealed trait ShopifyPartnerMessage extends PM

sealed case class ShopifyPartnerException(_message: String = "", _cause: Throwable = null) extends PE(_message, _cause)

import com.echoed.chamber.services.partner.shopify.{ShopifyPartnerMessage => SPM}
import com.echoed.chamber.services.partner.shopify.{ShopifyPartnerException => SPE}


case class RegisterShopifyPartnerEnvelope(
        shopifyPartner: ShopifyPartner,
        partner: Partner,
        partnerUser: PartnerUser,
        shopifyPartnerService: Option[ShopifyPartnerService] = None)

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
        with RM[RegisterShopifyPartnerEnvelope, RegisterShopifyPartner, PE]

case class Update(shopifyPartner: ShopifyPartner) extends SPM
case class UpdateResponse(message: Update, value: Either[SPE, ShopifyPartner])
        extends SPM
        with RM[ShopifyPartner, Update, SPE]




//case class LocateByPartnerId(partnerId: String) extends SPM
//
//case class LocateByPartnerIdResponse(message: LocateByPartnerId, value: Either[SPE, ShopifyPartnerService])
//        extends SPM
//        with RM[ShopifyPartnerService, LocateByPartnerId, SPE]
//
//case class LocateByToken(shop: String, signature: String, t: String, timeStamp: String) extends SPM
//
//case class LocateByTokenResponse(message: LocateByToken, value: Either[SPE, ShopifyPartnerService])
//        extends SPM
//        with RM[ShopifyPartnerService, LocateByToken, SPE]
//
//case class LocateByShopDomain(shopifyShopDomain: String) extends SPM
//
//case class LocateByShopDomainResponse(message: LocateByShopDomain, value: Either[SPE, ShopifyPartnerService])
//        extends SPM
//        with RM[ShopifyPartnerService, LocateByShopDomain, SPE]
//
//case class CreateFromPartnerId(partnerId: String) extends SPM
//
//case class CreateFromPartnerIdResponse(message: CreateFromPartnerId, value: Either[SPE, ShopifyPartnerService])
//        extends SPM
//        with RM[ShopifyPartnerService, CreateFromPartnerId, SPE]
//
//
//case class CreateFromToken(shop: String, signature: String, t: String, timeStamp: String) extends SPM
//
//case class CreateFromTokenResponse(message: CreateFromToken, value: Either[SPE, ShopifyPartnerService])
//        extends SPM
//        with RM[ShopifyPartnerService, CreateFromToken, SPE]
//
//case class CreateFromShopDomain(shopDomain: String) extends SPM
//
//case class CreateFromShopDomainResponse(message: CreateFromShopDomain, value: Either[SPE, ShopifyPartnerService])
//        extends SPM
//        with RM[ShopifyPartnerService, CreateFromShopDomain, SPE]


case class GetShopifyPartner() extends SPM
case class GetShopifyPartnerResponse(message: GetShopifyPartner, value: Either[SPE, ShopifyPartner])
        extends SPM
        with RM[ShopifyPartner, GetShopifyPartner, SPE]

case class GetOrder(orderId: Int) extends SPM
case class GetOrderResponse(message: GetOrder, value: Either[SPE, SO])
        extends SPM
        with RM[SO, GetOrder, SPE]

case class GetOrderFull(orderId: Int) extends SPM
case class GetOrderFullResponse(message: GetOrderFull, value: Either[SPE, ShopifyOrderFull])
        extends SPM
        with RM[ShopifyOrderFull, GetOrderFull, SPE]

case class GetProducts() extends SPM
case class GetProductsResponse(message: GetProducts, value: Either[SPE, List[SP]])
        extends SPM
        with RM[List[SP], GetProducts, SPE]


case class GetShop(password: String) extends SPM
case class GetShopResponse(message: GetShop, value: Either[SPE, ShopifyPartner])
        extends SPM
        with RM[ShopifyPartner, GetShop, SPE]


private[shopify] case class FetchPassword(shop: String, signature: String, t: String, timeStamp: String) extends SPM
private[shopify] case class FetchPasswordResponse(message: FetchPassword, value: Either[SPE, String])
        extends SPM
        with RM[String, FetchPassword, SPE]

private[shopify] case class FetchShop(shop: String, password: String) extends SPM
private[shopify] case class FetchShopResponse(message: FetchShop, value: Either[SPE, ShopifyPartner])
        extends SPM
        with RM[ShopifyPartner, FetchShop, SPE]

private[shopify] case class FetchOrder(shop: String, password: String, orderId: Int) extends SPM
private[shopify] case class FetchOrderResponse(message: FetchOrder, value: Either[SPE, SO])
        extends SPM
        with RM[SO, FetchOrder, SPE]

private[shopify] case class FetchProduct(shop: String, password: String, productId: Int) extends SPM
private[shopify] case class FetchProductResponse(message: FetchProduct, value: Either[SPE, SP])
        extends SPM
        with RM[SP, FetchProduct, SPE]

private[shopify] case class FetchProducts(shop: String, password: String) extends SPM
private[shopify] case class FetchProductsResponse(message: FetchProducts, value: Either[SPE, List[SP]])
        extends SPM
        with RM[List[SP], FetchProducts, SPE]

private[shopify] case class FetchShopFromToken(shop: String,  signature: String, t: String,  timeStamp: String) extends SPM
private[shopify] case class FetchShopFromTokenResponse(message: FetchShopFromToken, value: Either[SPE, ShopifyPartner])
        extends SPM
        with RM[ShopifyPartner, FetchShopFromToken, SPE]



