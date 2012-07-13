package com.echoed.chamber.services.event

import com.echoed.chamber.dao.EventLogDao
import akka.actor._
import com.echoed.chamber.domain.EventLog
import com.echoed.chamber.services.EchoedActor


class EventServiceActor(eventLogDao: EventLogDao) extends EchoedActor {

    def handle = {
        case msg @ Event(name, ref, refId) =>
            log.debug("Received {}", msg)
            eventLogDao.insert(new EventLog(name, ref, refId))
    }

}
