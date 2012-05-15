package com.echoed.chamber.services.partner.magentogo

import com.echoed.chamber.services.partner.PartnerServiceManager
import akka.dispatch.Future
import com.echoed.chamber.domain.magentogo.MagentoGoPartner

trait MagentoGoPartnerServiceManager {

    def registerPartner(partner: MagentoGoPartner): Future[RegisterMagentoGoPartnerResponse]

}
