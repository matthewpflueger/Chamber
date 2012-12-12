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
        override val correlationSender: Option[ActorRef]) extends PUM with Correlated[PartnerUserMessage with EmailIdentifiable]
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
case class GetPartnerUserResponse(message: GetPartnerUser, value: Either[PUE, PartnerUser])
        extends PUM with MR[PartnerUser, GetPartnerUser, PUE]

case class GetPartnerSettings(credentials: PUCC) extends PUM with PUI
case class GetPartnerSettingsResponse(message: GetPartnerSettings, value: Either[PUE, List[PartnerSettings]])
        extends PUM with MR[List[PartnerSettings], GetPartnerSettings, PUE]

case class RequestPartnerCustomization(credentials: PUCC) extends PUM with PUI
case class RequestPartnerCustomizationResponse(message: RequestPartnerCustomization, value: Either[PUE, Map[String, String]])
    extends PUM with MR[Map[String, String], RequestPartnerCustomization, PUE]

case class ActivatePartnerUser(credentials: PUCC, code: String, password: String) extends PUM with PUI
case class ActivatePartnerUserResponse(
        message: ActivatePartnerUser,
        value: Either[InvalidPassword, PUCC])
        extends PUM with MR[PUCC, ActivatePartnerUser, InvalidPassword]

case class UpdatePartnerUser(credentials: PUCC, name: String, email: String, password: String) extends PUM with PUI
case class UpdatePartnerUserResponse(
        message: UpdatePartnerUser,
        value: Either[InvalidPassword, PartnerUser])
        extends PUM with MR[PartnerUser, UpdatePartnerUser, InvalidPassword]

case class UpdatePartnerCustomization(
        credentials: PUCC,
        useGallery: Boolean,
        useRemote: Boolean,
        remoteVertical: String,
        remoteHorizontal: String,
        remoteOrientation: String,
        widgetTitle: String,
        widgetShareMessage: String) extends PUM with PUI
case class UpdatePartnerCustomizationResponse(
        message: UpdatePartnerCustomization,
        value: Either[PUE, Map[String, Any]])
        extends PUM with MR[Map[String, Any], UpdatePartnerCustomization, PUE]