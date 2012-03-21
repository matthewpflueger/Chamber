package com.echoed.chamber.services.shopify

import akka.actor.ActorRef
import com.echoed.chamber.domain._
import com.echoed.chamber.services.ActorClient


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/16/12
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */

class ShopifyUserServiceActorClient(shopifyUserServiceActor: ActorRef)
        extends ShopifyUserService
        with ActorClient
        with Serializable{

    def actorRef = shopifyUserServiceActor

    def getOrder(orderId: Int) =
        (shopifyUserServiceActor ? GetOrder(orderId)).mapTo[GetOrderResponse]
    
    def getShopifyUser() = 
        (shopifyUserServiceActor ? GetShopifyUser()).mapTo[GetShopifyUserResponse]

}
