package com.echoed.chamber.services.scheduler

import com.echoed.chamber.services.{EchoedException, MessageResponse => MR, Message}
import scala.collection.immutable.Stack


sealed trait SchedulerMessage extends Message

sealed case class SchedulerException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.scheduler.{SchedulerMessage => SM}
import com.echoed.chamber.services.scheduler.{SchedulerException => SE}


private[services] case class Schedule(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        schedulePattern: SchedulePattern,
        message: Message)


case object StartToday

private[services] case class SendToday(schedulesForToday: Map[String, Schedule])

//possible cron syntax implementations:
//http://www.sauronsoftware.it/projects/cron4j/
//http://kenai.com/projects/crontab-parser/pages/Home
//http://code.google.com/p/java-crontab-expression/
case class SchedulePattern(pattern: String)

object Today extends SchedulePattern("today")

case class ScheduleOnce(
        schedulePattern: SchedulePattern,
        message: Message,
        scheduleId: Option[String] = None) extends SM

case class ScheduleOnceResponse(
        message: ScheduleOnce,
        value: Either[SE, Schedule]) extends SM with MR[Schedule, ScheduleOnce, SE]
