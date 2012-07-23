package com.echoed.chamber.services

import akka.routing.{Destination, RouteeProvider, RouterConfig}
import akka.actor._
import akka.dispatch.Dispatchers
import akka.event.Logging
import com.google.common.cache.CacheBuilder
import scala.collection.JavaConversions._
import akka.pattern._
import akka.util.Timeout
import akka.routing.Destination


class MessageProcessorRouter(
        router: ActorRef,
        implicit val timeout: Timeout = Timeout(20000)) extends MessageProcessor {
    def apply(message: Message) = (router ? message).mapTo
}

class MessageRouter(routeMap: scala.collection.Map[Class[_ <: Message], ActorContext => ActorRef]) extends RouterConfig {

    def supervisorStrategy = SupervisorStrategy.defaultStrategy

    def routerDispatcher = Dispatchers.DefaultDispatcherId

    def createRoute(routeeProps: Props, routeeProvider: RouteeProvider) = {
        val log = Logging(routeeProvider.context.system, classOf[MessageRouter])

        val routes = asScalaConcurrentMap(CacheBuilder.newBuilder().concurrencyLevel(4).build[Class[_], ActorRef].asMap)
        routeMap.mapValues(_(routeeProvider.context)).foreach(kv => routes(kv._1) = kv._2)
        routeeProvider.registerRoutees(routes.values.toArray)
        log.debug("Successfully started")

        {
            case (sender, msg: Message) =>
                log.debug("Routing {}", msg)
                Array(Destination(sender, routes.getOrElseUpdate(msg.getClass, {
                    routes
                            .keys
                            .filter(_.isAssignableFrom(msg.getClass))
                            .headOption
                            .map(routes(_))
                            .getOrElse(throw new RuntimeException("No route for %s" format msg))
                })))
        }
    }

}
