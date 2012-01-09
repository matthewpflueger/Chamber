package com.echoed.chamber.services

import com.echoed.chamber.services.{ResponseMessage => RM}
import akka.util.Duration
import java.util.concurrent.TimeUnit
import akka.actor.{Channel, FSM, Actor, ActorRef}
import scalaz._
import Scalaz._
import scala.collection.mutable.{ListBuffer => MList}
import FSM._
import org.slf4j.LoggerFactory

sealed trait ScatterGatherState
case object Ready extends ScatterGatherState
case object Waiting extends ScatterGatherState


sealed trait ScatterGatherMessage extends Message
sealed case class ScatterGatherException(
    responses: List[Message],
    message: String = "",
    cause: Throwable = null) extends EchoedException(message, cause)


import com.echoed.chamber.services.{ScatterGatherMessage => SGM}
import com.echoed.chamber.services.{ScatterGatherException => SGE}


case class Timeout(res: List[Message], s: String = "", t: Throwable = null) extends SGE(res, s, t)
case class TimeoutTotal(r: List[Message], m: String = "", c: Throwable = null) extends Timeout(r, m, c)


case class Scatter(
        requestList: List[(ActorRef, Message)],
        context: Option[AnyRef],
        timeout: Duration = Duration(5, TimeUnit.SECONDS),
        timeoutTotal: Option[Duration] = None) extends SGM
case class ScatterResponse(message: Scatter, value: Either[SGE, List[Message]])
        extends SGM with RM[List[Message], Scatter, SGE]

private case class TimeoutTotalOccurred(timeoutTotal: Duration)


case class ScatterGatherData()
case class Gather(scatter: Scatter, channel: Channel[ScatterResponse], timeout: Duration) extends ScatterGatherData



class ScatterGather extends Actor with FSM[ScatterGatherState, ScatterGatherData] {

    private val logger = LoggerFactory.getLogger(classOf[ScatterGather])

    val responseList = MList[Message]()

    startWith(Ready, ScatterGatherData())

    when(Ready) {
        case Ev(scatter @ Scatter(requestList, context, timeout, timeoutTotal)) if (requestList.isEmpty) =>
            self.channel ! ScatterResponse(scatter, Right(List[Message]()))
            stop()

        case Ev(scatter @ Scatter(requestList, context, timeout, timeoutTotal)) =>
            requestList.foreach { tuple =>
                val (actorRef, message) = tuple
                actorRef ! message
            }

            val tt = timeoutTotal.getOrElse(timeout * requestList.size)
            setTimer(scatter.id, TimeoutTotalOccurred(tt), tt, false)
            goto(Waiting) forMax(timeout) using Gather(scatter, self.channel, timeout)
    }

    when(Waiting) {
        case Event(TimeoutTotalOccurred(timeoutTotal), Gather(scatter, channel, _)) =>
            channel ! ScatterResponse(
                scatter,
                Left(TimeoutTotal(
                    responseList.toList,
                    "Did not receive all messages within maximum time limit of %s" format timeoutTotal)))
            stop()

        case Event(StateTimeout, Gather(scatter, channel, timeout)) =>
            channel ! ScatterResponse(
                scatter,
                Left(Timeout(responseList.toList, "Did not receive any response within timeout %s" format timeout)))
            stop()

        case Event(response: Message, data @ Gather(scatter, channel, timeout)) =>
            responseList += response
            if (responseList.size == scatter.requestList.size) {
                channel ! ScatterResponse(scatter, Right(responseList.toList))
                cancelTimer(scatter.id)
                stop()
            } else {
                stay forMax(timeout) using data
            }
    }

}

