package com.echoed.chamber.services.partner.bigcommerce

import com.echoed.chamber.services.partner.PartnerServiceManager
import akka.dispatch.Future
import com.echoed.chamber.domain.bigcommerce.BigCommercePartner

trait BigCommercePartnerServiceManager {

    def registerPartner(partner: BigCommercePartner): Future[RegisterBigCommercePartnerResponse]

}
