package com.echoed.chamber.services.partner.shopify

import akka.actor.ActorRef
import com.echoed.chamber.services.partner.PartnerServiceActorClient
import com.echoed.chamber.domain.shopify.ShopifyPartner


class ShopifyPartnerServiceActorClient(actorRef: ActorRef)
        extends PartnerServiceActorClient(actorRef)
        with ShopifyPartnerService {

    def update(shopifyPartner: ShopifyPartner) =
        (actorRef ? Update(shopifyPartner)).mapTo[UpdateResponse]

    def getShopifyPartner() =
        (actorRef ? GetShopifyPartner()).mapTo[GetShopifyPartnerResponse]

}
