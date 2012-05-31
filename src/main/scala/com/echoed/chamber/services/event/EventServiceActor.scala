package com.echoed.chamber.services.event

import reflect.BeanProperty

import org.slf4j.LoggerFactory

import akka.actor.Actor
import com.echoed.chamber.dao.EventLogDao
import com.echoed.chamber.domain.EventLog


class EventServiceActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EventServiceActor])

    @BeanProperty var eventLogDao: EventLogDao = _

    def receive = {
        case msg @ Event(ref, refId, name) =>
            logger.debug("Received {}", msg)
            eventLogDao.insert(new EventLog(name, ref, refId))
    }

}
