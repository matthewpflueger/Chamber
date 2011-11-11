package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future


trait EchoedUserServiceLocator {
    def getEchoedUserServiceWithId(id: String): Future[EchoedUserService]
}