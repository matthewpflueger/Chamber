package com.echoed.chamber.services.partner.shopify

import com.echoed.chamber.services.partner.PartnerServiceManager
import akka.dispatch.Future

trait ShopifyPartnerServiceManager { //extends PartnerServiceManager {

    def registerShopifyPartner(shop: String, signature: String, t: String, timeStamp: String): Future[RegisterShopifyPartnerResponse]

}
