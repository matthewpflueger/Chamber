package com.echoed.chamber.services.event

import reflect.BeanProperty
import com.echoed.chamber.dao.EventLogDao
import com.echoed.chamber.domain.EventLog
import org.springframework.beans.factory.FactoryBean
import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.util.Timeout
import akka.util.duration._
import akka.event.Logging


class EventServiceActor extends FactoryBean[ActorRef] {

    @BeanProperty var eventLogDao: EventLogDao = _

    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    def receive = {
        case msg @ Event(ref, refId, name) =>
            logger.debug("Received {}", msg)
            eventLogDao.insert(new EventLog(name, ref, refId))
    }

    }), "EventService")
}
