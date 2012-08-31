package com.echoed.chamber.services.state.schema

import org.squeryl.KeyedEntity

import com.echoed.chamber.services.{Message, scheduler}
import com.echoed.chamber.services.scheduler.SchedulePattern
import com.echoed.util.ScalaObjectMapper

private[state] case class Schedule(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        schedulePattern: String,
        messageClass: String,
        message: String) extends KeyedEntity[String] {

    def this() = this("", 0L, 0L, "", "", "")

    def convertTo = scheduler.Schedule(
            id,
            updatedOn,
            createdOn,
            new SchedulePattern(schedulePattern),
            ScalaObjectMapper(message, Class.forName(messageClass)).asInstanceOf[Message])
}


private[state] object Schedule {
    def apply(s: scheduler.Schedule): Schedule = Schedule(
            s.id,
            s.updatedOn,
            s.createdOn,
            s.schedulePattern.pattern,
            s.message.getClass.getName,
            new ScalaObjectMapper().writeValueAsString(s.message))
}
