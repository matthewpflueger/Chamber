package com.echoed.chamber.services.partner.shopify

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient



class ShopifyAccessActorClient extends ShopifyAccess with ActorClient with Serializable {


    @BeanProperty var shopifyAccessActor: ActorRef = _

    def actorRef = shopifyAccessActor

    def fetchPassword(shop: String, signature: String, t: String, timeStamp: String) =
        (shopifyAccessActor ? FetchPassword(shop, signature, t, timeStamp)).mapTo[FetchPasswordResponse]

    def fetchOrder(shop: String, password: String, orderId: Int) =
        (shopifyAccessActor ? FetchOrder(shop, password, orderId)).mapTo[FetchOrderResponse]

    def fetchShop(shop: String, password: String) =
        (shopifyAccessActor ? FetchShop(shop, password)).mapTo[FetchShopResponse]

    def fetchProducts(shop: String, password: String) =
        (shopifyAccessActor ? FetchProducts(shop, password)).mapTo[FetchProductsResponse]

    def fetchShopFromToken(shop: String, signature: String, t: String, timeStamp: String) =
        (shopifyAccessActor ? FetchShopFromToken(shop, signature, t, timeStamp)).mapTo[FetchShopFromTokenResponse]
}
