package com.echoed.chamber.services.partner.shopify

import com.echoed.chamber.services.partner.PartnerServiceManagerActorClient

class ShopifyPartnerServiceManagerActorClient
        extends PartnerServiceManagerActorClient
        with ShopifyPartnerServiceManager {

    def registerShopifyPartner(shop: String, signature: String, t: String, timeStamp: String) =
        (actorRef ? RegisterShopifyPartner(shop, signature, t, timeStamp)).mapTo[RegisterShopifyPartnerResponse]
}
