package com.echoed.chamber.services.partner.shopify

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._



class ShopifyAccessActorClient extends ShopifyAccess with ActorClient with Serializable {

    @BeanProperty var shopifyAccessActor: ActorRef = _

    def actorRef = shopifyAccessActor

    private implicit val timeout = Timeout(20 seconds)

    def fetchPassword(shop: String, signature: String, t: String, timeStamp: String) =
        (shopifyAccessActor ? FetchPassword(shop, signature, t, timeStamp)).mapTo[FetchPasswordResponse]

    def fetchOrder(shop: String, password: String, orderId: Int) =
        (shopifyAccessActor ? FetchOrder(shop, password, orderId)).mapTo[FetchOrderResponse]

    def fetchShop(shop: String, password: String) =
        (shopifyAccessActor ? FetchShop(shop, password)).mapTo[FetchShopResponse]

    def fetchProducts(shop: String, password: String) =
        (shopifyAccessActor ? FetchProducts(shop, password)).mapTo[FetchProductsResponse]

    def fetchProduct(shop: String, password: String, productId: Int) =
        (shopifyAccessActor ? FetchProduct(shop, password, productId)).mapTo[FetchProductResponse]

    def fetchShopFromToken(shop: String, signature: String, t: String, timeStamp: String) =
        (shopifyAccessActor ? FetchShopFromToken(shop, signature, t, timeStamp)).mapTo[FetchShopFromTokenResponse]
}
