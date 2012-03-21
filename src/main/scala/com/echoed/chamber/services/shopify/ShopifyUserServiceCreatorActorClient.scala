package com.echoed.chamber.services.shopify

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.ActorClient


class ShopifyUserServiceCreatorActorClient
    extends ShopifyUserServiceCreator
    with ActorClient
    with Serializable{

    @BeanProperty var shopifyUserServiceCreatorActor: ActorRef = _

    def actorRef = shopifyUserServiceCreatorActor

    def createFromPartnerId(partnerId: String) =
        (shopifyUserServiceCreatorActor ? CreateFromPartnerId(partnerId)).mapTo[CreateFromPartnerIdResponse]


    def createFromToken(shop: String, signature: String, t: String, timeStamp: String) =
        (shopifyUserServiceCreatorActor ? CreateFromToken(shop, signature, t, timeStamp)).mapTo[CreateFromTokenResponse]
    
    def createFromShopDomain(shopifyShopDomain: String) =
        (shopifyUserServiceCreatorActor ? CreateFromShopDomain(shopifyShopDomain)).mapTo[CreateFromShopDomainResponse]


}
