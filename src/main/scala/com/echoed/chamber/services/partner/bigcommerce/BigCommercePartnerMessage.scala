package com.echoed.chamber.services.partner.bigcommerce

import com.echoed.chamber.services.{MessageResponse => MR}
import com.echoed.chamber.domain._
import partner.bigcommerce.{BigCommerceCredentials, BigCommercePartner}
import com.echoed.chamber.services.partner.{EchoRequest, PartnerAlreadyExists, PartnerException => PE, PartnerMessage => PM}
import partner.{PartnerUser, Partner}
import akka.actor.ActorRef

sealed trait BigCommercePartnerMessage extends PM

sealed case class BigCommercePartnerException(_message: String = "", _cause: Throwable = null) extends PE(_message, _cause)

import com.echoed.chamber.services.partner.bigcommerce.{BigCommercePartnerMessage => BCPM}
import com.echoed.chamber.services.partner.bigcommerce.{BigCommercePartnerException => BCPE}


case class RegisterBigCommercePartnerEnvelope(
        bigCommercePartner: BigCommercePartner,
        partner: Partner,
        partnerUser: PartnerUser,
        bigCommercePartnerService: Option[ActorRef] = None)

case class BigCommercePartnerAlreadyExists(
        envelope: RegisterBigCommercePartnerEnvelope,
        __message: String = "BigCommerce partner already exists",
        __cause: Throwable = null,
        __code: Option[String] = Some("bigcommerce.alreadyexists.partner"),
        __args: Option[Array[AnyRef]] = None) extends PartnerAlreadyExists(
    envelope.bigCommercePartnerService.get,
    __message,
    __cause,
    __code,
    __args)


case class RegisterBigCommercePartner(partner: BigCommercePartner) extends BCPM
case class RegisterBigCommercePartnerResponse(message: RegisterBigCommercePartner, value: Either[PE, RegisterBigCommercePartnerEnvelope])
        extends BCPM
        with MR[RegisterBigCommercePartnerEnvelope, RegisterBigCommercePartner, PE]


private[bigcommerce] case class Validate(credentials: BigCommerceCredentials) extends BCPM
private[bigcommerce] case class ValidateResponse(message: Validate, value: Either[BCPE, Boolean])
        extends BCPM
        with MR[Boolean, Validate, BCPE]

private[bigcommerce] case class FetchOrder(credentials: BigCommerceCredentials, orderId: Long) extends BCPM
private[bigcommerce] case class FetchOrderResponse(message: FetchOrder, value: Either[BCPE, EchoRequest])
        extends BCPM
        with MR[EchoRequest, FetchOrder, BCPE]



