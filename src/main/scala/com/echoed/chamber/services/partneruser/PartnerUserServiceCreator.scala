package com.echoed.chamber.services.partneruser

import akka.dispatch.Future

trait PartnerUserServiceCreator {

    def createPartnerUserService(email: String): Future[CreatePartnerUserServiceResponse]

}
