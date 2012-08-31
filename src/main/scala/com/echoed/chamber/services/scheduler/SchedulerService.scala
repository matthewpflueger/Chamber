package com.echoed.chamber.services.email

import akka.pattern._
import com.echoed.util.DateUtils._
import com.echoed.chamber.services._
import com.echoed.chamber.services.scheduler._
import akka.util.duration._
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
        sendIntervalInSeconds: Int = 5,
        sendImmediately: Boolean = false) extends OnlineOfflineService {

    assert(todayStartsAtHour > 0, "todayStartsAtHour is %s which is not greater than zero" format todayStartsAtHour)
    assert(sendIntervalInSeconds > 0, "sendIntervalInSeconds is %s which is not greater than zero" format sendIntervalInSeconds)

    private var schedules: Map[String, Schedule] = _

    private def scheduleStartToday {
        var todayStart = DateTime.now().withHourOfDay(todayStartsAtHour).withMinuteOfHour(0)
        if (todayStart.isBeforeNow) todayStart = todayStart.plusDays(1)
        context.system.scheduler.scheduleOnce(
                new JD(DateTime.now(), todayStart).getStandardMinutes minutes,
                context.self,
                StartToday)
    }

    override def lifespan = Unit

    override def preStart() {
        super.preStart()
        mp(ReadSchedulerServiceState()).pipeTo(self)
    }


    def init = {
        case ReadSchedulerServiceStateResponse(_, Right(s)) =>
            schedules = s
            becomeOnline
            if (DateTime.now().hourOfDay().get() == todayStartsAtHour) self ! StartToday
            else scheduleStartToday
    }


    def online = {
        case StartToday =>
            self ! SendToday(schedules)
            scheduleStartToday


        case SendToday(schedulesForToday) => schedulesForToday.headOption.foreach { case (id, _) =>
            schedules.get(id).foreach { schedule =>
                schedules = schedules - id
                mp(schedule.message)
                ep(ScheduleDeleted(schedule))
            }

            context.system.scheduler.scheduleOnce(
                    sendIntervalInSeconds seconds,
                    self,
                    SendToday(schedulesForToday - id))
        }


        case msg @ ScheduleOnce(Today, message, id) =>
            val sid = id.getOrElse(UUID())
            val schedule = schedules.get(sid).getOrElse {
                val schedule = Schedule(sid, new Date, new Date, Today, message)
                schedules = schedules + (sid -> schedule)
                ep(ScheduleCreated(schedule))
                if (sendImmediately) self ! StartToday
                schedule
            }
            sender ! ScheduleOnceResponse(msg, Right(schedule))


        case msg: ScheduleOnce =>
            sender ! ScheduleOnceResponse(
                    msg,
                    Left(SchedulerException("Unsupported schedule pattern %s" format msg.schedulePattern)))
    }

}
