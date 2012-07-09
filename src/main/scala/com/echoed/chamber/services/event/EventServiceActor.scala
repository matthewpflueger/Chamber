package com.echoed.chamber.services.event

import com.echoed.chamber.dao.EventLogDao
import akka.actor._
import com.echoed.chamber.domain.EventLog


class EventServiceActor(eventLogDao: EventLogDao) extends Actor with ActorLogging {

    def receive = {
        case msg @ Event(name, ref, refId) =>
            log.debug("Received {}", msg)
            eventLogDao.insert(new EventLog(name, ref, refId))
    }

}
