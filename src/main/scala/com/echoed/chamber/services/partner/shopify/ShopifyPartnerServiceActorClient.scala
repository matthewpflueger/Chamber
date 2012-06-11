package com.echoed.chamber.services.partner.shopify

import akka.actor.ActorRef
import com.echoed.chamber.services.partner.PartnerServiceActorClient
import com.echoed.chamber.domain.partner.shopify.ShopifyPartner
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class ShopifyPartnerServiceActorClient(actorRef: ActorRef)
        extends PartnerServiceActorClient(actorRef)
        with ShopifyPartnerService {

    private implicit val timeout = Timeout(20 seconds)

    def update(shopifyPartner: ShopifyPartner) =
        (actorRef ? Update(shopifyPartner)).mapTo[UpdateResponse]

    def getShopifyPartner() =
        (actorRef ? GetShopifyPartner()).mapTo[GetShopifyPartnerResponse]

}
