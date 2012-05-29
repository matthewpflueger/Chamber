package com.echoed.chamber.services.partner.magentogo

import akka.dispatch.Future
import com.echoed.chamber.domain.partner.magentogo.MagentoGoPartner

trait MagentoGoPartnerServiceManager {

    def registerPartner(partner: MagentoGoPartner): Future[RegisterMagentoGoPartnerResponse]

}
