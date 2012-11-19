package com.echoed.chamber.services.event

import com.echoed.chamber.domain.EventLog
import com.echoed.chamber.services.EchoedService


class EventService extends EchoedService {

    def handle = {
        //we are not receiving events and nobody cares so disabling...
        case msg @ Event(name, ref, refId) =>
            log.error("Received {} but not persisted!!!", msg)
//            eventLogDao.insert(new EventLog(name, ref, refId))
    }

}
