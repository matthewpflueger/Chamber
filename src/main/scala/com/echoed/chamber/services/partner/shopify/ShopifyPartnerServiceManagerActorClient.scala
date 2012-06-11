package com.echoed.chamber.services.partner.shopify

import com.echoed.chamber.services.partner.PartnerServiceManagerActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

class ShopifyPartnerServiceManagerActorClient
        extends PartnerServiceManagerActorClient
        with ShopifyPartnerServiceManager {

    private implicit val timeout = Timeout(20 seconds)

    def registerShopifyPartner(shop: String, signature: String, t: String, timeStamp: String) =
        (actorRef ? RegisterShopifyPartner(shop, signature, t, timeStamp)).mapTo[RegisterShopifyPartnerResponse]
}
