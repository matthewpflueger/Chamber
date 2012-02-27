package com.echoed.chamber.services.partner

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.{RetailerSettings, Retailer, RetailerUser, FacebookComment}
import com.echoed.chamber.domain.views._


sealed trait PartnerMessage extends Message

sealed case class PartnerException(
        message: String = "",
        cause: Throwable = null,
        code: Option[String] = None,
        arguments: Option[Array[AnyRef]] = None) extends EchoedException(message, cause, code, arguments)

import com.echoed.chamber.services.partner.{PartnerMessage => PM}
import com.echoed.chamber.services.partner.{PartnerException => PE}


case class RegisterPartner(partner: Retailer, partnerSettings: RetailerSettings, partnerUser: RetailerUser) extends PM
case class RegisterPartnerResponse(
        message: RegisterPartner,
        value: Either[PE, Retailer]) extends PM with RM[Retailer, RegisterPartner, PE]

case class Locate(partnerId: String) extends PM
case class LocateResponse(
        message: Locate,
        value: Either[PE, PartnerService]) extends PM with RM[PartnerService, Locate, PE]

case class PartnerNotFound(
        partnerId: String,
        _message: String = "Partner not found",
        _cause: Throwable = null,
        _code: Option[String] = Some("notfound.partner"),
        _args: Option[Array[AnyRef]] = None) extends PartnerException(_message, _cause, _code, _args) {
    def this(partnerId: String) = this(partnerId, _args = Some(Array(partnerId)))
}

case class PartnerNotActive(
        partnerId: String,
        _message: String = "Partner not active",
        _cause: Throwable = null,
        _code: Option[String] = Some("notfound.partner"),
        _args: Option[Array[AnyRef]] = None) extends PartnerException(_message, _cause, _code, _args) {
    def this(partnerId: String) = this(partnerId, _args = Some(Array(partnerId)))
}

case class InvalidEchoRequest(_message: String = "Invalid echo request",
                              _cause: Throwable = null,
                              _code: Option[String] = Some("invalid.echoRequest"),
                              _args: Option[Array[AnyRef]] = None) extends PartnerException(_message, _cause, _code, _args)

case class RequestEcho(
        request: String,
        ipAddress: String,
        echoedUserId: Option[String] = None,
        echoClickId: Option[String] = None) extends PM
case class RequestEchoResponse(
        message: RequestEcho,
        value: Either[PE, EchoPossibilityView]) extends PM with RM[EchoPossibilityView, RequestEcho, PE]

