package com.echoed.chamber.services

import akka.event.LoggingReceive

abstract class ContentOnlineOfflineService extends OnlineOfflineService{

    protected var contentLoaded = false

    protected var customContentLoaded = false

    protected def becomeCustomContentLoaded = {
        customContentLoaded = true
        unhandledMessages.reverse.foreach{
            tuple =>
                log.debug("Replaying {}", tuple._1)
                self.tell(tuple._1, tuple._2)
        }

    }

    protected def becomeContentLoaded = {
        contentLoaded = true
        unhandledMessages.reverse.foreach {
            tuple =>
                log.debug("Replaying {}", tuple._1)
                self.tell(tuple._1, tuple._2)
        }

    }


}
