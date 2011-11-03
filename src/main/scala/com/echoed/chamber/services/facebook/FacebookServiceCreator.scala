package com.echoed.chamber.services.facebook

import akka.dispatch.{CompletableFuture, Future}


trait FacebookServiceCreator {
    def createFacebookServiceUsingCode(code: String): Future[FacebookService]
}