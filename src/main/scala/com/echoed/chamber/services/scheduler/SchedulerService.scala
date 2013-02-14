package com.echoed.chamber.services.email

import com.echoed.util.DateUtils._
import com.echoed.chamber.services._
import com.echoed.chamber.services.scheduler._
import scala.concurrent.duration._
import com.echoed.util.UUID
import java.util.Date
import com.echoed.chamber.services.scheduler.ScheduleOnceResponse
import com.echoed.chamber.services.state.ReadSchedulerServiceState
import com.echoed.chamber.services.scheduler.ScheduleOnce
import com.echoed.chamber.services.state.ReadSchedulerServiceStateResponse
import com.echoed.chamber.services.scheduler.Schedule
import scala.Right
import org.joda.time.{Duration => JD, DateTime}



class SchedulerService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        todayStartsAtHour: Int = 20,
        hourStartsAtMinute: Int = 30,
        weekStartsAtDay: Int = 2, //Tuesday, Monday = 1, Sunday = 7
        sendIntervalInSeconds: Int = 5,
        sendImmediately: Boolean = false) extends OnlineOfflineService {

    import context.dispatcher

    assert(weekStartsAtDay > 0, "weekStartsAtDay is %s which is not greater than zero" format weekStartsAtDay)
    assert(todayStartsAtHour > 0, "todayStartsAtHour is %s which is not greater than zero" format todayStartsAtHour)
    assert(hourStartsAtMinute > 0, "hourStartsAtMinute is %s which is not greater than zero" format hourStartsAtMinute)
    assert(sendIntervalInSeconds > 0, "sendIntervalInSeconds is %s which is not greater than zero" format sendIntervalInSeconds)

    private var schedules: Map[String, Schedule] = _

    private def differenceInMinutes(dt: DateTime) = new JD(DateTime.now(), dt).getStandardMinutes

    private def scheduleStartToday {
        var todayStart = DateTime.now()
                .withHourOfDay(todayStartsAtHour)
                .withMinuteOfHour(hourStartsAtMinute)
        if (differenceInMinutes(todayStart) < 1) todayStart = todayStart.plusDays(1)
        context.system.scheduler.scheduleOnce(
                differenceInMinutes(todayStart).minutes,
                context.self,
                StartToday)
    }

    private def scheduleStartHour {
        val hourStart = DateTime.now().plusHours(1).withMinuteOfHour(hourStartsAtMinute)
        context.system.scheduler.scheduleOnce(
                differenceInMinutes(hourStart).minutes,
                context.self,
                StartHour)
    }


    private def scheduleStartWeek {
        var weekStart = DateTime.now()
                .withDayOfWeek(weekStartsAtDay)
                .withHourOfDay(todayStartsAtHour)
                .withMinuteOfHour(hourStartsAtMinute)
        if (differenceInMinutes(weekStart) < 1) weekStart = weekStart.plusWeeks(1)
        context.system.scheduler.scheduleOnce(
                differenceInMinutes(weekStart).minutes,
                context.self,
                StartWeek)
    }


    override def lifespan = Unit

    override def preStart() {
        super.preStart()
        mp.tell(ReadSchedulerServiceState(), self)
    }


    def init = {
        case ReadSchedulerServiceStateResponse(_, Right(s)) =>
            schedules = s
            becomeOnline
            if (DateTime.now().hourOfDay().get() == todayStartsAtHour) self ! StartToday
            else scheduleStartToday
            scheduleStartHour
            scheduleStartWeek
    }


    def online = {
        case StartToday =>
            self ! Send(schedules.filter(t => t._2.schedulePattern == Today))
            scheduleStartToday

        case StartHour =>
            self ! Send(schedules.filter(t => t._2.schedulePattern == Hour))
            scheduleStartHour

        case StartWeek =>
            self ! Send(schedules.filter(t => t._2.schedulePattern == Week))
            scheduleStartWeek

        case Send(schedulesToSend) => schedulesToSend.headOption.foreach { case (id, _) =>
            schedules.get(id).foreach { schedule =>
                schedules = schedules - id
                mp(schedule.message)
                ep(ScheduleDeleted(schedule))
            }

            context.system.scheduler.scheduleOnce(
                    sendIntervalInSeconds.seconds,
                    self,
                    Send(schedulesToSend - id))
        }


        case msg @ ScheduleOnce(pattern, message, id) if (pattern == Today || pattern == Hour || pattern == Week) =>
            val sid = id.getOrElse(UUID())
            val schedule = schedules.get(sid).getOrElse {
                val schedule = Schedule(sid, new Date, new Date, pattern, message)
                schedules = schedules + (sid -> schedule)
                ep(ScheduleCreated(schedule))
                if (sendImmediately) self ! Send(schedules)
                schedule
            }
            sender ! ScheduleOnceResponse(msg, Right(schedule))

        case msg: ScheduleOnce =>
            sender ! ScheduleOnceResponse(
                    msg,
                    Left(SchedulerException("Unsupported schedule pattern %s" format msg.schedulePattern)))
    }

}
