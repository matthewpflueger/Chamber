package com.echoed.chamber.services.facebook

import akka.dispatch.Future


trait FacebookServiceLocator {

    def getFacebookServiceWithCode(code: String, queryString: String): Future[FacebookService]

    def getFacebookServiceWithFacebookUserId(facebookUserId: String): Future[FacebookService]

}
