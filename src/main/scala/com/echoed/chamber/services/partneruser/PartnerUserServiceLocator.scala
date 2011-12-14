package com.echoed.chamber.services.partneruser

import akka.dispatch.Future


trait PartnerUserServiceLocator {

    def login(email:String, password: String): Future[LoginResponse]

    def locate(partnerUserId: String): Future[LocateResponse]
}


