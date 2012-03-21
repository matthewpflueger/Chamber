package com.echoed.chamber.services.shopify

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.ActorClient


class ShopifyUserServiceLocatorActorClient
    extends ShopifyUserServiceLocator
    with ActorClient
    with Serializable{

    @BeanProperty var shopifyUserServiceLocatorActor: ActorRef = _

    def actorRef = shopifyUserServiceLocatorActor
    
    def locate(shop: String, signature: String, t: String, timeStamp: String) =
        (shopifyUserServiceLocatorActor ? LocateByToken(shop, signature, t, timeStamp)).mapTo[LocateByTokenResponse]
    
    def locateByPartnerId(partnerId: String) = 
        (shopifyUserServiceLocatorActor ? LocateByPartnerId(partnerId)).mapTo[LocateByPartnerIdResponse]

}
