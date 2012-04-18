package com.echoed.chamber.services.shopify

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain._
import com.shopify.api.credentials.Credential
import com.shopify.api.resources.{Order => SO, Product=> SP}
import shopify.{ShopifyOrderFull, ShopifyPartner}

sealed trait ShopifyMessage extends Message

sealed case class ShopifyException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.shopify.{ShopifyMessage => SM}
import com.echoed.chamber.services.shopify.{ShopifyException => SE}

case class LocateByPartnerId(partnerId: String) extends SM
case class LocateByPartnerIdResponse(message: LocateByPartnerId, value: Either[SE, ShopifyUserService])
    extends SM 
    with RM[ShopifyUserService, LocateByPartnerId, SE]

case class LocateByToken(shop: String,  signature: String, t: String,  timeStamp: String) extends SM
case class LocateByTokenResponse(message: LocateByToken,  value: Either[SE, ShopifyUserService])
    extends SM 
    with RM[ShopifyUserService, LocateByToken,  SE]

case class LocateByShopDomain(shopifyShopDomain: String) extends SM
case class LocateByShopDomainResponse(message: LocateByShopDomain, value: Either[SE, ShopifyUserService])
    extends SM 
    with RM[ShopifyUserService, LocateByShopDomain, SE]

case class CreateFromPartnerId(partnerId: String) extends SM
case class CreateFromPartnerIdResponse(message: CreateFromPartnerId, value: Either[SE, ShopifyUserService])
    extends SM
    with RM[ShopifyUserService, CreateFromPartnerId, SE]


case class CreateFromToken(shop: String, signature: String,  t: String,  timeStamp:String) extends SM
case class CreateFromTokenResponse(message: CreateFromToken, value: Either[SE, ShopifyUserService])
    extends SM
    with RM[ShopifyUserService, CreateFromToken, SE]

case class CreateFromShopDomain(shopDomain: String) extends SM
case class CreateFromShopDomainResponse(message: CreateFromShopDomain, value: Either[SE,  ShopifyUserService])
    extends SM
    with RM[ShopifyUserService, CreateFromShopDomain, SE]


//Shopify User Service

case class GetOrder(orderId: Int) extends SM
case class GetOrderResponse(message: GetOrder,  value: Either[SE,  SO])
    extends SM
    with RM[SO,  GetOrder,  SE]

case class GetOrderFull(orderId: Int) extends SM
case class GetOrderFullResponse(message: GetOrderFull,  value: Either[SE, ShopifyOrderFull])
    extends SM
    with RM[ShopifyOrderFull, GetOrderFull, SE]

case class GetProducts() extends SM
case class GetProductsResponse(message: GetProducts,  value: Either[SE, List[SP]])
    extends SM
    with RM[List[SP], GetProducts, SE]


case class GetShop(password: String) extends SM
case class GetShopResponse(message: GetShop, value: Either[SE, ShopifyPartner])
    extends SM
    with RM[ShopifyPartner, GetShop, SE]

case class GetShopifyUser() extends SM
case class GetShopifyUserResponse(message: GetShopifyUser,  value: Either[SE, ShopifyPartner])
    extends SM
    with RM[ShopifyPartner,  GetShopifyUser, SE]


//Shopify Accesss


case class FetchPassword(shop: String,  signature: String, t: String,  timeStamp: String) extends SM
case class FetchPasswordResponse(message: FetchPassword, value: Either[SE, String])
    extends SM
    with RM[String, FetchPassword, SE]

case class FetchShop(shop: String, password: String) extends SM
case class FetchShopResponse(message: FetchShop, value: Either[SE, ShopifyPartner])
    extends SM
    with RM[ShopifyPartner,  FetchShop, SE]

case class FetchOrder(shop:String, password: String, orderId: Int) extends SM
case class FetchOrderResponse(message: FetchOrder,  value: Either[SE,  SO])
    extends SM
    with RM[SO, FetchOrder, SE]

case class FetchProduct(shop: String, password: String, productId: Int) extends SM
case class FetchProductResponse(message: FetchProduct, value: Either[SE,  SP])
    extends SM
    with RM[SP, FetchProduct, SE]

case class FetchProducts(shop: String, password: String) extends SM
case class FetchProductsResponse(message: FetchProducts, value: Either[SE, List[SP]])
    extends SM 
    with RM[List[SP],  FetchProducts,  SE]


