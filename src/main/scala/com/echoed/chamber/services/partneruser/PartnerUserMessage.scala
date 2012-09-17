package com.echoed.chamber.services.partneruser

import com.echoed.chamber.services.{MessageResponse => MR, Correlated, EchoedClientCredentials, EchoedException, Message}
import com.echoed.chamber.domain.partner._
import com.echoed.chamber.domain._
import akka.actor.ActorRef
import com.echoed.chamber.services.partner.PartnerIdentifiable


sealed trait PartnerUserMessage extends Message
sealed case class PartnerUserException(
        message: String = "",
        cause: Throwable = null,
        code: Option[String] = None) extends EchoedException(message, cause, code)


case class PartnerUserClientCredentials(
        id: String,
        name: Option[String] = None,
        email: Option[String] = None,
        partnerId: Option[String] = None,
        partnerName: Option[String] = None) extends EchoedClientCredentials {

    def partnerUserId = id
}

trait PartnerUserIdentifiable {
    this: PartnerUserMessage =>
    def credentials: PartnerUserClientCredentials
    def partnerUserId = credentials.partnerUserId
}

trait EmailIdentifiable {
    this: PartnerUserMessage =>
    def email: String
}


import com.echoed.chamber.services.partneruser.{PartnerUserMessage => PUM}
import com.echoed.chamber.services.partneruser.{PartnerUserException => PUE}
import com.echoed.chamber.services.partneruser.{PartnerUserClientCredentials => PUCC}
import com.echoed.chamber.services.partneruser.{PartnerUserIdentifiable => PUI}


private[partneruser] case class RegisterPartnerUserService(partnerUser: PartnerUser) extends PUM

private[partneruser] case class LoginWithCredentials(credentials: PUCC) extends PUM with PUI

private[partneruser] case class LoginWithEmail(
        email: String,
        correlation: PartnerUserMessage with EmailIdentifiable,
        override val correlationSender: Option[ActorRef]) extends PUM with Correlated
private[partneruser] case class LoginWithEmailResponse(message: LoginWithEmail, value: Either[PUE, PartnerUser])
    extends PUM with MR[PartnerUser, LoginWithEmail, PUE]


case class InvalidCredentials(m: String = "Invalid email or password", c: Throwable = null) extends PUE(m, c)
case class LoginWithEmailPassword(email: String, password: String) extends PUM with EmailIdentifiable
case class LoginWithEmailPasswordResponse(message: LoginWithEmailPassword, value: Either[PUE, PartnerUser])
        extends PUM with MR[PartnerUser, LoginWithEmailPassword, PUE]

case class LoginWithCode(code: String) extends PUM
case class LoginWithCodeResponse(message: LoginWithCode, value: Either[PUE, PartnerUser])
    extends PUM with MR[PartnerUser, LoginWithCode, PUE]


case class Logout(credentials: PartnerUserClientCredentials) extends PUM with PUI


case class GetPartnerUser(credentials: PUCC) extends PUM with PUI
case class GetPartnerUserResponse(message: GetPartnerUser, value: Either[PartnerUserException, PartnerUser])
        extends PUM with MR[PartnerUser, GetPartnerUser, PUE]

case class GetPartnerSettings(credentials: PUCC) extends PUM with PUI
case class GetPartnerSettingsResponse(message: GetPartnerSettings, value: Either[PartnerUserException, List[PartnerSettings]])
        extends PUM with MR[List[PartnerSettings], GetPartnerSettings, PUE]

case class ActivatePartnerUser(credentials: PUCC, password: String) extends PUM with PUI
case class ActivatePartnerUserResponse(
        message: ActivatePartnerUser,
        value: Either[InvalidPassword, PartnerUser])
        extends PUM with MR[PartnerUser, ActivatePartnerUser, InvalidPassword]

