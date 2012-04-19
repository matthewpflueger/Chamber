package com.echoed.chamber.services.partner.shopify

import com.echoed.chamber.services.partner.PartnerService
import com.echoed.chamber.domain.shopify.ShopifyPartner
import akka.dispatch.Future

trait ShopifyPartnerService extends PartnerService {

    def getShopifyPartner: Future[GetShopifyPartnerResponse]

    def update(shopifyPartner: ShopifyPartner): Future[UpdateResponse]

}
