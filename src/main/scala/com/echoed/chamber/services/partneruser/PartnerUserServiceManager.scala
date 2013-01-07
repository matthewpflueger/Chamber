package com.echoed.chamber.services.partneruser

import akka.actor._
import akka.util.Timeout
import com.echoed.chamber.services._
import com.echoed.chamber.domain.Identifiable
import com.google.common.collect.HashMultimap
import akka.actor.Terminated
import scala.collection.JavaConversions._
import scalaz._
import Scalaz._
import com.echoed.util.{Encrypter, ScalaObjectMapper}


class PartnerUserServiceManager(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        partnerUserServiceCreator: (ActorContext, Message) => ActorRef,
        encrypter: Encrypter,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {

    import context.dispatcher

    private val active = HashMultimap.create[Identifiable, ActorRef]()


    def handle = {
        case Terminated(ref) => active.values.removeAll(active.values.filter(_ == ref))

        case RegisterPartnerUserService(partnerUser) =>
            active.put(PartnerUserId(partnerUser.id), context.sender)
            active.put(Email(partnerUser.email), context.sender)

        case msg @ LoginWithCode(code) =>
            val channel = context.sender
            val map = ScalaObjectMapper(encrypter.decrypt(code), classOf[Map[String, String]])
            mp(LoginWithEmailPassword(map("email"), map("password"))).onSuccess {
                case LoginWithEmailPasswordResponse(_, Left(e)) =>
                    channel ! LoginWithCodeResponse(msg, Left(e))
                case LoginWithEmailPasswordResponse(_, Right(echoedUser)) =>
                    channel ! LoginWithCodeResponse(msg, Right(echoedUser))
            }

        case msg: EmailIdentifiable with PartnerUserMessage =>
            active.get(Email(msg.email)).headOption.cata(
                _.forward(msg),
                {
                    val partnerUserService = context.watch(partnerUserServiceCreator(context, LoginWithEmail(msg.email, msg, Option(sender))))
                    partnerUserService.forward(msg)
                    active.put(Email(msg.email), partnerUserService)
                })


        case msg: PartnerUserIdentifiable =>
            active.get(PartnerUserId(msg.partnerUserId)).headOption.cata(
                _.forward(msg),
                {
                    val partnerUserService = context.watch(partnerUserServiceCreator(context, LoginWithCredentials(msg.credentials)))
                    partnerUserService.forward(msg)
                    active.put(PartnerUserId(msg.partnerUserId), partnerUserService)
                })

    }

}

case class PartnerUserId(id: String) extends Identifiable
case class Email(id: String) extends Identifiable
