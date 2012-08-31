package com.echoed.chamber.services.partneruser

import com.echoed.chamber.dao.partner.PartnerUserDao
import com.echoed.chamber.dao.views.PartnerViewDao
import com.echoed.chamber.domain.partner.{PartnerUser}
import scala.collection.JavaConversions._
import java.util.ArrayList
import scalaz._
import Scalaz._
import akka.actor.PoisonPill
import akka.pattern._
import com.echoed.chamber.services._
import scala.Left
import scala.Right
import com.echoed.chamber.services.state.{ReadPartnerUserForCredentialsResponse, ReadPartnerUserForEmailResponse, ReadPartnerUserForCredentials, ReadPartnerUserForEmail}


class PartnerUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        partnerUserDao: PartnerUserDao,
        partnerViewDao: PartnerViewDao) extends OnlineOfflineService {

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
            val channel = context.sender

            try {
                partnerUser = partnerUser.createPassword(password)
                partnerUserDao.updatePassword(partnerUser)
                channel ! ActivatePartnerUserResponse(msg, Right(partnerUser))
            } catch {
                case e: InvalidPassword =>
                    channel ! ActivatePartnerUserResponse(msg, Left(e))
                case e =>
                    channel ! ActivatePartnerUserResponse(
                            msg,
                            Left(PartnerUserException("Could not activate partner user %s" format partnerUser.name, e)))
            }

        case msg @ Logout(_) =>
            self ! PoisonPill

        case msg: GetPartnerUser =>
            sender ! GetPartnerUserResponse(msg, Right(partnerUser))

        case msg: GetPartnerSettings =>
            Option(partnerViewDao.getPartnerSettings(partnerUser.partnerId)).cata(
                resultSet => sender ! GetPartnerSettingsResponse(msg, Right(asScalaBuffer(resultSet).toList)),
                sender ! GetPartnerSettingsResponse(msg, Left(PartnerUserException("Partner Settings not available"))))

    }

}
