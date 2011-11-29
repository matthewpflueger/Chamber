package com.echoed.chamber.services.facebook

import akka.dispatch.Future


trait FacebookServiceCreator {

    def createFacebookServiceUsingCode(code: String): Future[FacebookService]

    def createFacebookServiceUsingFacebookUserId(facebookUserId: String): Future[FacebookService]

}
