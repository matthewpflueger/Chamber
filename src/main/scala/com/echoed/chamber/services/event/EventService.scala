package com.echoed.chamber.services.event

import com.echoed.chamber.dao.EventLogDao
import com.echoed.chamber.domain.EventLog
import com.echoed.chamber.services.EchoedService


class EventService(eventLogDao: EventLogDao) extends EchoedService {

    def handle = {
        case msg @ Event(name, ref, refId) =>
            log.debug("Received {}", msg)
            eventLogDao.insert(new EventLog(name, ref, refId))
    }

}
