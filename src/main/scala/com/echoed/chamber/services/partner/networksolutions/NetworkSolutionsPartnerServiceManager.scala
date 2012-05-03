package com.echoed.chamber.services.partner.networksolutions

import com.echoed.chamber.services.partner.PartnerServiceManager
import akka.dispatch.Future

trait NetworkSolutionsPartnerServiceManager {


    def registerPartner(
            name: String,
            email: String,
            phone: String,
            successUrl: String,
            failureUrl: Option[String] = None): Future[RegisterNetworkSolutionsPartnerResponse]

    def authPartner(userKey: String): Future[AuthNetworkSolutionsPartnerResponse]

}
