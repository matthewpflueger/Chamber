package com.echoed.chamber.services

import akka.event.LoggingReceive

abstract class ContentOnlineOfflineService extends OnlineOfflineService{

    protected def contentLoaded: Receive

    protected def customContentLoaded: Receive

    protected def becomeCustomContentLoaded = {
        context.become(LoggingReceive(customContentLoaded.orElse(receiveTimeout)), false)
        unhandledMessages.reverse.foreach{
            tuple =>
                log.debug("Replaying {}", tuple._1)
                self.tell(tuple._1, tuple._2)
        }

    }

    protected def becomeContentLoaded = {
        context.become(LoggingReceive(contentLoaded.orElse(receiveTimeout)), false)
        unhandledMessages.reverse.foreach {
            tuple =>
                log.debug("Replaying {}", tuple._1)
                self.tell(tuple._1, tuple._2)
        }

    }


}
