package com.echoed.chamber.services.partneruser

import akka.dispatch.Future


trait PartnerUserServiceLocator {

    def login(email:String, password: String): Future[LoginResponse]

    def logout(id: String): Future[LogoutResponse]

    def locate(partnerUserId: String): Future[LocateResponse]
}


