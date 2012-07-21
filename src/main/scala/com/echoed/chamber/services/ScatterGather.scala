package com.echoed.chamber.services

import com.echoed.chamber.services.{MessageResponse => MR}
import akka.util.Duration
import java.util.concurrent.TimeUnit
import akka.actor.{FSM, Actor, ActorRef}
import scala.collection.mutable.{ListBuffer => MList}

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


case class GatherTimeout(res: List[Message], s: String = "", t: Throwable = null) extends SGE(res, s, t)
case class GatherTimeoutTotal(r: List[Message], m: String = "", c: Throwable = null) extends GatherTimeout(r, m, c)


case class Scatter(
        requestList: List[(ActorRef, Message)],
        context: Option[AnyRef],
        timeout: Duration = Duration(5, TimeUnit.SECONDS),
        timeoutTotal: Option[Duration] = None) extends SGM
case class ScatterResponse(message: Scatter, value: Either[SGE, List[Message]])
        extends SGM with MR[List[Message], Scatter, SGE]

private case class TimeoutTotalOccurred(timeoutTotal: Duration)


case class ScatterGatherData()
case class Gather(scatter: Scatter, channel: ActorRef, timeout: Duration) extends ScatterGatherData



class ScatterGather extends Actor with FSM[ScatterGatherState, ScatterGatherData] {

    val responseList = MList[Message]()

    startWith(Ready, ScatterGatherData())

    when(Ready) {
        case Event(scatter @ Scatter(requestList, context, timeout, timeoutTotal), _) if (requestList.isEmpty) =>
            log.warning("Empty request list received")
            sender ! ScatterResponse(scatter, Right(List[Message]()))
            stop()

        case Event(scatter @ Scatter(requestList, context, timeout, timeoutTotal), _) =>
            log.debug("Recevied request list of size {}", requestList.size)
            requestList.foreach { tuple =>
                val (actorRef, message) = tuple
                actorRef ! message
            }

            val tt = timeoutTotal.getOrElse(timeout * requestList.size)
            setTimer(scatter.id, TimeoutTotalOccurred(tt), tt, false)
            goto(Waiting) forMax(timeout) using Gather(scatter, sender, timeout)
    }

    when(Waiting) {
        case Event(TimeoutTotalOccurred(timeoutTotal), Gather(scatter, channel, _)) =>
            channel ! ScatterResponse(
                scatter,
                Left(GatherTimeoutTotal(
                    responseList.toList,
                    "Did not receive all messages within maximum time limit of %s" format timeoutTotal)))
            stop()

        case Event(StateTimeout, Gather(scatter, channel, timeout)) =>
            channel ! ScatterResponse(
                scatter,
                Left(GatherTimeout(
                    responseList.toList,
                    "Did not receive any response within timeout %s" format timeout)))
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

