package com.echoed.chamber.services.shopify

import reflect.BeanProperty
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}
import akka.actor.{Actor, ActorRef}
import com.echoed.chamber.services.ActorClient


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/16/12
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */

class ShopifyAccessActorClient
    extends ShopifyAccess
    with ActorClient
    with Serializable {

    @BeanProperty var shopifyAccessActor: ActorRef = _

    def actorRef = shopifyAccessActor
    
    def fetchPassword(shop: String, signature:String, t:String, timeStamp:String ) =
        (shopifyAccessActor ? FetchPassword(shop, signature, t, timeStamp)).mapTo[FetchPasswordResponse]
    
    def fetchOrder(shop: String, password: String,  orderId: Int) =
        (shopifyAccessActor ? FetchOrder(shop, password, orderId)).mapTo[FetchOrderResponse]

    def fetchShop(shop: String, password: String) =
        (shopifyAccessActor ? FetchShop(shop, password)).mapTo[FetchShopResponse]
    
}
