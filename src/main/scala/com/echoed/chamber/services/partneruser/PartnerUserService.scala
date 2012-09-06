package com.echoed.chamber.services.partneruser

import com.echoed.chamber.domain.partner.{PartnerUser}
import akka.actor.PoisonPill
import akka.pattern._
import com.echoed.chamber.services._
import scala.Left
import scala.Right
import com.echoed.chamber.services.state.{ReadPartnerUserForCredentialsResponse, ReadPartnerUserForEmailResponse, ReadPartnerUserForCredentials, ReadPartnerUserForEmail}
import com.echoed.chamber.domain.InvalidPassword
import com.echoed.chamber.services.partner.PartnerClientCredentials


class PartnerUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message) extends OnlineOfflineService {

    private var partnerUser: PartnerUser = _

    private def setStateAndRegister(pu: PartnerUser) {
        partnerUser = pu
        becomeOnline
        context.parent ! RegisterPartnerUserService(partnerUser)
    }

    override def preStart() {
        super.preStart()
        initMessage match {
            case LoginWithEmail(email, _, _) => mp(ReadPartnerUserForEmail(email)).pipeTo(self)
            case LoginWithCredentials(credentials) => mp(ReadPartnerUserForCredentials(credentials)).pipeTo(self)
        }
    }

    def init = {
        case msg @ ReadPartnerUserForEmailResponse(_, Left(_)) => initMessage match {
            case LoginWithEmail(_, msg @ LoginWithEmailPassword(_, _), channel) =>
                channel.get ! LoginWithEmailPasswordResponse(msg, Left(InvalidCredentials())); self ! PoisonPill
        }

        case msg @ ReadPartnerUserForEmailResponse(_, Right(pu)) => setStateAndRegister(pu)
        case msg @ ReadPartnerUserForCredentialsResponse(_, Right(pu)) => setStateAndRegister(pu)
    }

    def online = {
        case msg @ LoginWithEmailPassword(email, password) =>
            if (partnerUser.isCredentials(email, password)) sender ! LoginWithEmailPasswordResponse(msg, Right(partnerUser))
            else sender ! LoginWithEmailPasswordResponse(msg, Left(InvalidCredentials()))

        case msg @ ActivatePartnerUser(_, password) =>
            try {
                partnerUser = partnerUser.createPassword(password)
                sender ! ActivatePartnerUserResponse(msg, Right(partnerUser))
                ep(PartnerUserUpdated(partnerUser))
            } catch {
                case e: InvalidPassword => sender ! ActivatePartnerUserResponse(msg, Left(e))
            }

        case msg @ Logout(_) =>
            self ! PoisonPill

        case msg: GetPartnerUser =>
            sender ! GetPartnerUserResponse(msg, Right(partnerUser))

        case msg: GetPartnerSettings =>
            val channel = sender

            mp(partner.GetPartnerSettings(new PartnerClientCredentials with EchoedClientCredentials {
                val id = partnerUser.partnerId
            })).onSuccess {
                case GetPartnerSettingsResponse(_, Right(ps)) => channel ! GetPartnerSettingsResponse(msg, Right(ps))
            }

    }

}
