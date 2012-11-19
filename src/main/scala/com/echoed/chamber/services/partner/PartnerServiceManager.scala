package com.echoed.chamber.services.partner

import scalaz._
import Scalaz._
import com.echoed.chamber.services.{Message, MessageProcessor, EchoedService}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.SupervisorStrategy.Stop
import com.google.common.collect.HashMultimap
import scala.collection.JavaConversions._


class PartnerServiceManager(
        mp: MessageProcessor,
        partnerServiceCreator: (ActorContext, Message) => ActorRef,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {


    private val active = HashMultimap.create[String, ActorRef]()

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 0) {
        case _: Throwable ⇒ Stop
    }


    def handle = {
        case Terminated(ref) => active.values.removeAll(active.values.filter(_ == ref))

        case msg @ RegisterPartnerService(partner) =>
            active.put(partner.id, sender)

        case msg: PartnerIdentifiable with PartnerMessage =>
            active.get(msg.credentials.id).headOption.cata(
                _.forward(msg),
                {
                    val partnerService = context.watch(partnerServiceCreator(context, msg))
                    partnerService.forward(msg)
                    active.put(msg.credentials.id, partnerService)
                })

        case msg: RegisterPartner => context.watch(partnerServiceCreator(context, msg))
    }


}



