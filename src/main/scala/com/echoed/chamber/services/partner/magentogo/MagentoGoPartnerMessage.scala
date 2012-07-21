package com.echoed.chamber.services.partner.magentogo

import com.echoed.chamber.services.{MessageResponse => MR}
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.partner.magentogo.{MagentoGoCredentials, MagentoGoPartner}
import com.echoed.chamber.services.partner.{EchoRequest, PartnerAlreadyExists, PartnerException => PE, PartnerMessage => PM}
import partner.{PartnerUser, Partner}

sealed trait MagentoGoPartnerMessage extends PM

sealed case class MagentoGoPartnerException(_message: String = "", _cause: Throwable = null) extends PE(_message, _cause)

import com.echoed.chamber.services.partner.magentogo.{MagentoGoPartnerMessage => MGPM}
import com.echoed.chamber.services.partner.magentogo.{MagentoGoPartnerException => MGPE}

case class SessionTimeout(credentials: MagentoGoCredentials)
        extends MGPE("MagentoGo session timeout for %s at %s" format(credentials.apiPath, credentials.apiUser))

case class RegisterMagentoGoPartnerEnvelope(
        magentoGoPartner: MagentoGoPartner,
        partner: Partner,
        partnerUser: PartnerUser,
        magentoGoPartnerService: Option[MagentoGoPartnerService] = None)

case class MagentoGoPartnerAlreadyExists(
        envelope: RegisterMagentoGoPartnerEnvelope,
        __message: String = "MagentoGo partner already exists",
        __cause: Throwable = null,
        __code: Option[String] = Some("magentogo.alreadyexists.partner"),
        __args: Option[Array[AnyRef]] = None) extends PartnerAlreadyExists(
    envelope.magentoGoPartnerService.get,
    __message,
    __cause,
    __code,
    __args)


case class RegisterMagentoGoPartner(partner: MagentoGoPartner) extends MGPM
case class RegisterMagentoGoPartnerResponse(message: RegisterMagentoGoPartner, value: Either[PE, RegisterMagentoGoPartnerEnvelope])
        extends MGPM
        with MR[RegisterMagentoGoPartnerEnvelope, RegisterMagentoGoPartner, PE]


private[magentogo] case class Validate(credentials: MagentoGoCredentials) extends MGPM
private[magentogo] case class ValidateResponse(message: Validate, value: Either[MGPE, String])
        extends MGPM
        with MR[String, Validate, MGPE]

private[magentogo] case class FetchOrder(credentials: MagentoGoCredentials, orderId: Long) extends MGPM
private[magentogo] case class FetchOrderResponse(message: FetchOrder, value: Either[MGPE, EchoRequest])
        extends MGPM
        with MR[EchoRequest, FetchOrder, MGPE]



