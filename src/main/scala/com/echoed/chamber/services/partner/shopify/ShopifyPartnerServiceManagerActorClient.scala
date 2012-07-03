package com.echoed.chamber.services.partner.shopify

import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import com.echoed.chamber.services.ActorClient
import scala.reflect.BeanProperty
import akka.actor.ActorRef

class ShopifyPartnerServiceManagerActorClient extends ShopifyPartnerServiceManager with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def registerShopifyPartner(shop: String, signature: String, t: String, timeStamp: String) =
        (actorRef ? RegisterShopifyPartner(shop, signature, t, timeStamp)).mapTo[RegisterShopifyPartnerResponse]

}
