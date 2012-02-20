package com.echoed.chamber.services.partner

import akka.dispatch.Future
import com.echoed.chamber.domain.{RetailerSettings, RetailerUser, Retailer}


trait PartnerService {

    def registerPartner(
            partner: Retailer,
            partnerSettings: RetailerSettings,
            partnerUser: RetailerUser): Future[RegisterPartnerResponse]

}

