package com.echoed.chamber.services.partner

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.shopify.ShopifyOrderFull
import com.echoed.chamber.domain.views._
import com.echoed.chamber.domain._


sealed trait PartnerMessage extends Message

sealed case class PartnerException(
        message: String = "",
        cause: Throwable = null,
        code: Option[String] = None,
        arguments: Option[Array[AnyRef]] = None) extends EchoedException(message, cause, code, arguments)

import com.echoed.chamber.services.partner.{PartnerMessage => PM}
import com.echoed.chamber.services.partner.{PartnerException => PE}


case class RegisterPartner(partner: Partner, partnerSettings: PartnerSettings, partnerUser: PartnerUser) extends PM
case class RegisterPartnerResponse(
        message: RegisterPartner,
        value: Either[PE, Partner]) extends PM with RM[Partner, RegisterPartner, PE]

case class UpdatePartner(partner: Partner, partnerSettings: PartnerSettings, partnerUser: PartnerUser) extends PM
case class UpdatePartnerResponse(
        message: UpdatePartner, 
        value: Either[PE, Partner]) extends PM with RM[Partner, UpdatePartner, PE]

case class UpdatePartnerSettings(partnerSettings: PartnerSettings) extends PM
case class UpdatePartnerSettingsResponse(
        message: UpdatePartnerSettings, 
        value: Either[PE, PartnerSettings]) extends PM with RM[PartnerSettings,  UpdatePartnerSettings, PE]


case class Locate(partnerId: String) extends PM
case class LocateResponse(
        message: Locate,
        value: Either[PE, PartnerService]) extends PM with RM[PartnerService, Locate, PE]


case class LocateByEchoId(echoId: String) extends PM
case class LocateByEchoIdResponse(
        message: LocateByEchoId,
        value: Either[PE, PartnerService]) extends PM with RM[PartnerService, LocateByEchoId, PE]


case class PartnerNotFound(
        partnerId: String,
        _message: String = "Partner not found",
        _cause: Throwable = null,
        _code: Option[String] = Some("notfound.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args) {
    def this(partnerId: String) = this(partnerId, _args = Some(Array(partnerId)))
}


case class PartnerAlreadyExists(
        partnerId: String,
        partnerUser: PartnerUser,
        _message: String = "Partner Already Exists",
        _cause: Throwable = null,
        _code: Option[String] = Some("alreadyexists.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args) {
    def this(partnerId: String, partnerUser: PartnerUser) = this(partnerId, partnerUser, _args = Some(Array(partnerId)))
}

case class PartnerNotActive(
        partnerId: String,
        _message: String = "Partner not active",
        _cause: Throwable = null,
        _code: Option[String] = Some("notfound.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args) {
    def this(partnerId: String) = this(partnerId, _args = Some(Array(partnerId)))
}


case class InvalidEchoRequest(_message: String = "Invalid echo request",
                              _cause: Throwable = null,
                              _code: Option[String] = Some("invalid.echoRequest"),
                              _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args)


case class GetPartner() extends PM
case class GetPartnerResponse(
        message: GetPartner, 
        value: Either[PE, Partner]) extends PM with RM[Partner, GetPartner,  PE]

case class GetPartnerSettings() extends PM
case class GetPartnerSettingsResponse(
        message: GetPartnerSettings,
        value: Either[PE, PartnerSettings]) extends PM with RM[PartnerSettings, GetPartnerSettings, PE]

case class RequestShopifyEcho(
        order: ShopifyOrderFull,
        browserId: String,
        ipAddress: String,
        userAgent: String,
        referrerUrl: String,
        echoedUserId: Option[String] = None,
        echoClickId: Option[String] = None) extends PM
case class RequestShopifyEchoResponse(
        message: RequestShopifyEcho,
        value: Either[PE, EchoPossibilityView]) extends PM with RM[EchoPossibilityView, RequestShopifyEcho, PE]

case class RequestEcho(
        request: String,
        browserId: String,
        ipAddress: String,
        userAgent: String,
        referrerUrl: String,
        echoedUserId: Option[String] = None,
        echoClickId: Option[String] = None,
        view: Option[String] = None) extends PM
case class RequestEchoResponse(
        message: RequestEcho,
        value: Either[PE, EchoPossibilityView]) extends PM with RM[EchoPossibilityView, RequestEcho, PE]


case class RecordEchoStep(
        echoId: String,
        step: String,
        echoedUserId: Option[String] = None,
        echoClickId: Option[String] = None) extends PM
case class RecordEchoStepResponse(
        message: RecordEchoStep,
        value: Either[PE, EchoPossibilityView]) extends PM with RM[EchoPossibilityView, RecordEchoStep, PE]


case class EchoExists(
        echoPossibilityView: EchoPossibilityView,
        _message: String = "Item already echoed",
        _cause: Throwable = null) extends PE(_message, _cause)

case class EchoNotFound(id: String, m: String = "Echo not found") extends PE(m)

