package com.echoed.chamber.services.scheduler

import com.echoed.chamber.services.{DeletedEvent, CreatedEvent, Event}

trait SchedulerEvent extends Event

import com.echoed.chamber.services.scheduler.{SchedulerEvent => SE}

private[services] case class ScheduleCreated(schedule: Schedule) extends SE with CreatedEvent
private[services] case class ScheduleDeleted(schedule: Schedule) extends SE with DeletedEvent
