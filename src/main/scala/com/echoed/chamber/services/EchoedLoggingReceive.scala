package com.echoed.chamber.services

import akka.actor.ActorContext
import akka.event.{LogSource, LoggingReceive}
import akka.actor.Actor.Receive
import akka.event.Logging.Debug


/* Complete rip from akka.event.LoggingReceive */
object EchoedLoggingReceive {

    def apply(source: Option[AnyRef] = None, r: Receive)(implicit context: ActorContext): Receive = r match {
        case _: EchoedLoggingReceive => r
        case _ => if (context.system.settings.AddLoggingReceive) new EchoedLoggingReceive(source, r) else r
    }
}


class EchoedLoggingReceive(source: Option[AnyRef], r: Receive)(implicit context: ActorContext) extends Receive {
    def isDefinedAt(o: Any): Boolean = {
        val handled = r.isDefinedAt(o)
        val (str, clazz) = LogSource.fromAnyRef(source getOrElse context.self)
        val message = o match {
            case msg: MessageGroup[_] => msg.getClass.getSimpleName + " consisting of " + msg.messages.length + " messages"
            case msg => msg.toString.take(100)
        }
        context.system.eventStream.publish(Debug(str, clazz, (if (handled) "handled " else "unhandled ") + message))
        handled
    }
    def apply(o: Any): Unit = r(o)
}
