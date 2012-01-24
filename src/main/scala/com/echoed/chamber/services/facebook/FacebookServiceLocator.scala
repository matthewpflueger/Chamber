package com.echoed.chamber.services.facebook

import akka.dispatch.Future


trait FacebookServiceLocator {

    def locateByCode(code: String, queryString: String): Future[LocateByCodeResponse]

    def locateById(facebookUserId: String): Future[LocateByIdResponse]

    def locateByFacebookId(facebookId: String, accessToken: String): Future[LocateByFacebookIdResponse]

    def logout(facebookUserId: String): Future[LogoutResponse]
}
