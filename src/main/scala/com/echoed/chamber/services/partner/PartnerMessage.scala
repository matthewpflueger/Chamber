package com.echoed.chamber.services.partner

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.views._
import com.echoed.chamber.domain._
import partner.{PartnerSettings, PartnerUser, Partner}
import akka.actor.ActorRef


trait PartnerMessage extends Message

case class PartnerException(
        message: String = "",
        cause: Throwable = null,
        code: Option[String] = None,
        arguments: Option[Array[AnyRef]] = None) extends EchoedException(message, cause, code, arguments)

import com.echoed.chamber.services.partner.{PartnerMessage => PM}
import com.echoed.chamber.services.partner.{PartnerException => PE}


abstract case class PartnerIdentifiable(partnerId: String) extends PM
abstract case class EchoIdentifiable(echoId: String) extends PM

import com.echoed.chamber.services.partner.{PartnerIdentifiable => PI}
import com.echoed.chamber.services.partner.{EchoIdentifiable => EI}


private[partner] case class Create(msg: Locate, channel: ActorRef)

case class RegisterPartner(partner: Partner, partnerSettings: PartnerSettings, partnerUser: PartnerUser) extends PM
case class RegisterPartnerResponse(
        message: RegisterPartner,
        value: Either[PE, PartnerService]) extends PM with RM[PartnerService, RegisterPartner, PE]

case class UpdatePartner(partner: Partner, partnerSettings: PartnerSettings, partnerUser: PartnerUser) extends PM
case class UpdatePartnerResponse(
        message: UpdatePartner, 
        value: Either[PE, Partner]) extends PM with RM[Partner, UpdatePartner, PE]

case class GetPartner() extends PM
case class GetPartnerResponse(
        message: GetPartner,
        value: Either[PE, Partner]) extends PM with RM[Partner, GetPartner, PE]

case class Locate(partnerId: String) extends PM
case class LocateResponse(
        message: Locate,
        value: Either[PE, PartnerService]) extends PM with RM[PartnerService, Locate, PE]


case class LocateByEchoId(echoId: String) extends PM
case class LocateByEchoIdResponse(
        message: LocateByEchoId,
        value: Either[PE, PartnerService]) extends PM with RM[PartnerService, LocateByEchoId, PE]


case class LocateByDomain(domain: String, context: Option[AnyRef] = None) extends PM
case class LocateByDomainResponse(
        message: LocateByDomain,
        value: Either[PE, PartnerService]) extends PM with RM[PartnerService, LocateByDomain, PE]


case class ViewDescription(view: String, model: Map[String, Any])

case class GetView(_partnerId: String) extends PI(_partnerId)
case class GetViewResponse(
        message: GetView,
        value: Either[PE, ViewDescription]) extends PM with RM[ViewDescription, GetView, PE]



case class PartnerNotFound(
        partnerId: String,
        _message: String = "Partner not found",
        _cause: Throwable = null,
        _code: Option[String] = Some("notfound.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args) {
    def this(partnerId: String) = this(partnerId, _args = Some(Array(partnerId)))
}


case class PartnerAlreadyExists(
        partnerService: PartnerService,
        _message: String = "Partner already exists",
        _cause: Throwable = null,
        _code: Option[String] = Some("alreadyexists.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args)

case class PartnerNotActive(
        partner: Partner,
        _message: String = "Partner %s (%s) is not active",
        _cause: Throwable = null,
        _code: Option[String] = Some("notfound.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args) {
    def this(partner: Partner) = this(partner, _args = Some(Array(partner.id, partner.name)))
    override def getMessage = _message format(partner.name, partner.id)
}


case class InvalidEchoRequest(_message: String = "Invalid echo request",
                              _cause: Throwable = null,
                              _code: Option[String] = Some("invalid.echoRequest"),
                              _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args)


//case class GetPartner() extends PM
//case class GetPartnerResponse(
//        message: GetPartner,
//        value: Either[PE, Partner]) extends PM with RM[Partner, GetPartner,  PE]
//
//case class GetPartnerSettings() extends PM
//case class GetPartnerSettingsResponse(
//        message: GetPartnerSettings,
//        value: Either[PE, PartnerSettings]) extends PM with RM[PartnerSettings, GetPartnerSettings, PE]
//
//case class RequestShopifyEcho(
//        order: ShopifyOrderFull,
//        browserId: String,
//        ipAddress: String,
//        userAgent: String,
//        referrerUrl: String,
//        echoedUserId: Option[String] = None,
//        echoClickId: Option[String] = None) extends PM
//case class RequestShopifyEchoResponse(
//        message: RequestShopifyEcho,
//        value: Either[PE, EchoPossibilityView]) extends PM with RM[EchoPossibilityView, RequestShopifyEcho, PE]

case class RequestEcho(
        _partnerId: String,
        request: String,
        browserId: String,
        ipAddress: String,
        userAgent: String,
        referrerUrl: String,
        echoedUserId: Option[String] = None,
        echoClickId: Option[String] = None,
        view: Option[String] = None) extends PI(_partnerId)
case class RequestEchoResponse(
        message: RequestEcho,
        value: Either[PE, EchoPossibilityView]) extends PM with RM[EchoPossibilityView, RequestEcho, PE]

case class GetEcho(__echoId: String) extends EI(__echoId)
case class GetEchoResponse(
        message: GetEcho,
        value: Either[PE, EchoPossibilityView]) extends PM with RM[EchoPossibilityView, GetEcho, PE]


case class RecordEchoStep(
        _echoId: String,
        step: String,
        echoedUserId: Option[String] = None,
        echoClickId: Option[String] = None) extends EI(_echoId)
case class RecordEchoStepResponse(
        message: RecordEchoStep,
        value: Either[PE, EchoPossibilityView]) extends PM with RM[EchoPossibilityView, RecordEchoStep, PE]


case class EchoExists(
        echoPossibilityView: EchoPossibilityView,
        _message: String = "Item already echoed",
        _cause: Throwable = null) extends PE(_message, _cause)

case class EchoNotFound(id: String, m: String = "Echo not found %s") extends PE(m format id)



