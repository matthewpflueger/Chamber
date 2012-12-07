package com.echoed.chamber.services.partneruser

import com.echoed.chamber.domain.partner.{PartnerUser}
import akka.actor.PoisonPill
import akka.pattern._
import com.echoed.chamber.services._
import com.echoed.chamber.services.partner.{FetchPartnerAndPartnerSettingsResponse, FetchPartnerAndPartnerSettings, PartnerClientCredentials, PutPartnerCustomization}
import scala.Left
import scala.Right
import com.echoed.chamber.services.state.{ReadPartnerUserForCredentialsResponse, ReadPartnerUserForEmailResponse, ReadPartnerUserForCredentials, ReadPartnerUserForEmail}
import com.echoed.chamber.domain.InvalidPassword
import partner._
import com.echoed.util.DateUtils._
import java.util.Date
import com.echoed.util.{Encrypter, ScalaObjectMapper}
import scala.Left
import com.echoed.chamber.domain.partner.PartnerUser
import state.ReadPartnerUserForCredentials
import state.ReadPartnerUserForCredentialsResponse
import scala.Right
import com.echoed.chamber.domain.InvalidPassword
import state.ReadPartnerUserForEmail
import state.ReadPartnerUserForEmailResponse


class PartnerUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        encrypter: Encrypter,
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
            case LoginWithEmail(email, _, _) => mp.tell(ReadPartnerUserForEmail(email), self)
            case LoginWithCredentials(credentials) => mp.tell(ReadPartnerUserForCredentials(credentials), self)
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

        case msg @ ActivatePartnerUser(_, code, password) =>
            try {
                val map = ScalaObjectMapper(encrypter.decrypt(code), classOf[Map[String, String]])
                map.get("password").filter(partnerUser.isPassword(_)).foreach { _ =>
                    partnerUser = partnerUser.copy(updatedOn = new Date).createPassword(password)
                    sender ! ActivatePartnerUserResponse(
                        msg,
                        Right(new PartnerUserClientCredentials(
                            partnerUser.id,
                            Option(partnerUser.name),
                            Option(partnerUser.email),
                            Option(partnerUser.partnerId))))
                    ep(PartnerUserUpdated(partnerUser))
                }
            } catch {
                case e: InvalidPassword => sender ! ActivatePartnerUserResponse(msg, Left(e))
            }

        case msg @ UpdatePartnerUser(_, name, email, password) =>
            try {
                partnerUser = partnerUser.copy(name = name, email = email, updatedOn = new Date).createPassword(password)
                sender ! UpdatePartnerUserResponse(msg, Right(partnerUser))
                ep(PartnerUserUpdated(partnerUser))
            } catch {
                case e: InvalidPassword => sender ! UpdatePartnerUserResponse(msg, Left(e))
            }

        case msg @ Logout(_) =>
            self ! PoisonPill

        case msg: GetPartnerUser =>
            sender ! GetPartnerUserResponse(msg, Right(partnerUser))

        case msg: GetPartnerSettings =>
            mp(FetchPartnerAndPartnerSettings(PartnerClientCredentials(partnerUser.partnerId)))
                .mapTo[FetchPartnerAndPartnerSettingsResponse]
                .map(_.resultOrException)
                .map(r => GetPartnerSettingsResponse(msg, Right(List(r.partnerSettings))))
                .pipeTo(sender)

        case msg @ UpdatePartnerCustomization(_, useGallery, useRemote, remoteVertical, remoteHorizontal, remoteOrientation) =>
            val channel = context.sender
            mp(PutPartnerCustomization(
                PartnerClientCredentials(partnerUser.partnerId),
                useGallery,
                useRemote,
                remoteVertical,
                remoteHorizontal,
                remoteOrientation)).onSuccess {
                case PutPartnerCustomizationResponse(_, Right(customization)) =>
                    channel ! UpdatePartnerCustomizationResponse(msg, Right(customization))

            }
    }

}
