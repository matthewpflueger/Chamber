package com.echoed.chamber.services.partner.networksolutions

import com.echoed.chamber.services.{MessageResponse => MR}
import java.util.Date
import com.echoed.chamber.services.partner.{EchoRequest, PartnerAlreadyExists, PartnerException => PEx, PartnerMessage => PM}
import com.echoed.chamber.domain._
import partner.networksolutions.NetworkSolutionsPartner
import partner.{PartnerUser, Partner}

sealed trait NetworkSolutionsPartnerMessage extends PM

sealed case class NetworkSolutionsPartnerException(_message: String = "", _cause: Throwable = null) extends PEx(_message, _cause)

import com.echoed.chamber.services.partner.networksolutions.{NetworkSolutionsPartnerMessage => NSPM}
import com.echoed.chamber.services.partner.networksolutions.{NetworkSolutionsPartnerException => NSPE}


case class NetworkSolutionsPartnerEnvelope(
        networkSolutionsPartner: NetworkSolutionsPartner,
        partner: Partner,
        partnerUser: PartnerUser,
        networkSolutionsPartnerService: Option[NetworkSolutionsPartnerService] = None)

case class NetworkSolutionsPartnerAlreadyExists(
        envelope: NetworkSolutionsPartnerEnvelope,
        __message: String = "Network Solutions partner already exists",
        __cause: Throwable = null,
        __code: Option[String] = Some("networksolutions.alreadyexists.partner"),
        __args: Option[Array[AnyRef]] = None) extends PartnerAlreadyExists(
            envelope.networkSolutionsPartnerService.get,
            __message,
            __cause,
            __code,
            __args)

case class RegisterNetworkSolutionsPartner(
        name: String,
        email: String,
        phone: String,
        successUrl: String,
        failureUrl: Option[String] = None) extends NSPM
case class RegisterNetworkSolutionsPartnerResponse(message: RegisterNetworkSolutionsPartner, value: Either[PEx, String])
        extends NSPM
        with MR[String, RegisterNetworkSolutionsPartner, PEx]


case class AuthNetworkSolutionsPartner(userKey: String) extends NSPM
case class AuthNetworkSolutionsPartnerResponse(message: AuthNetworkSolutionsPartner, value: Either[PEx, NetworkSolutionsPartnerEnvelope])
        extends NSPM
        with MR[NetworkSolutionsPartnerEnvelope, AuthNetworkSolutionsPartner, PEx]


case class FetchUserKeyEnvelope(loginUrl: String, userKey: String)

private[networksolutions] case class FetchUserKey(
        successUrl: String,
        failureUrl: Option[String] = None) extends NSPM
private[networksolutions] case class FetchUserKeyResponse(message: FetchUserKey, value: Either[NSPE, FetchUserKeyEnvelope])
        extends NSPM
        with MR[FetchUserKeyEnvelope, FetchUserKey, NSPE]


case class FetchUserTokenEnvelope(
        userToken: String,
        expiresOn: Date,
        companyName: String,
        storeUrl: String,
        secureStoreUrl: String)

private[networksolutions] case class FetchUserToken(userKey: String) extends NSPM
private[networksolutions] case class FetchUserTokenResponse(
        message: FetchUserToken,
        value: Either[NSPE, FetchUserTokenEnvelope])
        extends NSPM
        with MR[FetchUserTokenEnvelope, FetchUserToken, NSPE]


private[networksolutions] case class FetchOrder(
        userToken: String,
        orderNumber: Long) extends NSPM
private[networksolutions] case class FetchOrderResponse(message: FetchOrder, value: Either[NSPE, EchoRequest])
        extends NSPM
        with MR[EchoRequest, FetchOrder, NSPE]

