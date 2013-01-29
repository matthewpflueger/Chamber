package com.echoed.chamber.services

import akka.actor.ActorContext
import akka.event.{LogSource, LoggingReceive}
import akka.actor.Actor.Receive
import akka.event.Logging.Debug


/* Complete rip from akka.event.LoggingReceive */
object EchoedLoggingReceive {

    def apply(r: Receive)(implicit context: ActorContext): Receive = r match {
        case _: EchoedLoggingReceive => r
        case _ => if (context.system.settings.AddLoggingReceive) new EchoedLoggingReceive(None, r) else r
    }
}


class EchoedLoggingReceive(source: Option[AnyRef], r: Receive)(implicit context: ActorContext) extends LoggingReceive(source, r) {
    override def isDefinedAt(o: Any): Boolean = {
        val handled = r.isDefinedAt(o)
        if (o.isInstanceOf[OnlineOnlyMessage]) handled
        else super.isDefinedAt(o)
    }
    override def apply(o: Any): Unit = r(o)
}
