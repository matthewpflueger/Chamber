package com.echoed.chamber.services.facebook

import akka.dispatch.Future


trait FacebookServiceLocator {
    def getFacebookServiceWithCode(code: String): Future[FacebookService]
}