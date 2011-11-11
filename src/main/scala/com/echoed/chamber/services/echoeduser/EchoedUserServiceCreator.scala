package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future


trait EchoedUserServiceCreator {
    def createEchoedUserServiceUsingId(id: String): Future[EchoedUserService]
}