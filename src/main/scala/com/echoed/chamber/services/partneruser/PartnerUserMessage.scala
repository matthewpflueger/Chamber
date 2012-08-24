package com.echoed.chamber.services.partneruser

import com.echoed.chamber.services.{MessageResponse => MR, Correlated, EchoedClientCredentials, EchoedException, Message}
import com.echoed.chamber.domain.views.{PartnerSocialSummary, ProductSocialSummary, PartnerProductsListView, PartnerCustomerListView, PartnerProductSocialActivityByDate, PartnerSocialActivityByDate,CustomerSocialSummary,PartnerCustomerSocialActivityByDate, PartnerEchoView}
import com.echoed.chamber.domain.partner._
import com.echoed.chamber.domain._
import akka.actor.ActorRef


sealed trait PartnerUserMessage extends Message
sealed case class PartnerUserException(
        message: String = "",
        cause: Throwable = null,
        code: Option[String] = None) extends EchoedException(message, cause, code)

trait PartnerUserClientCredentials {
    this: EchoedClientCredentials =>
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
case class LogoutResponse(message: Logout, value: Either[PUE, Boolean])
    extends PUM with MR[Boolean, Logout, PUE]


case class GetPartnerUser(credentials: PUCC) extends PUM with PUI
case class GetPartnerUserResponse(message: GetPartnerUser, value: Either[PartnerUserException, PartnerUser])
        extends PUM with MR[PartnerUser, GetPartnerUser, PUE]

case class GetPartnerSettings(credentials: PUCC) extends PUM with PUI
case class GetPartnerSettingsResponse(message: GetPartnerSettings, value: Either[PartnerUserException, List[PartnerSettings]])
        extends PUM with MR[List[PartnerSettings], GetPartnerSettings, PUE]

case class GetCustomerSocialSummary(credentials: PUCC, echoedUserId: String) extends PUM with PUI
case class GetCustomerSocialSummaryResponse(message: GetCustomerSocialSummary, value: Either[PartnerUserException, CustomerSocialSummary])
        extends PUM with MR[CustomerSocialSummary, GetCustomerSocialSummary, PUE]

case class GetCustomerSocialActivityByDate(credentials: PUCC, echoedUserId: String) extends PUM with PUI
case class GetCustomerSocialActivityByDateResponse(message: GetCustomerSocialActivityByDate, value: Either[PartnerUserException, PartnerCustomerSocialActivityByDate])
        extends PUM with MR[PartnerCustomerSocialActivityByDate, GetCustomerSocialActivityByDate, PUE]

case class GetPartnerSocialSummary(credentials: PUCC) extends PUM with PUI
case class GetPartnerSocialSummaryResponse(message: GetPartnerSocialSummary, value: Either[PartnerUserException, PartnerSocialSummary])
        extends PUM with MR[PartnerSocialSummary, GetPartnerSocialSummary, PUE]

case class GetEchoClickGeoLocation(credentials: PUCC) extends PUM with PUI
case class GetEchoClickGeoLocationResponse(message: GetEchoClickGeoLocation, value: Either[PartnerUserException, List[GeoLocation]])
        extends PUM with MR[List[GeoLocation], GetEchoClickGeoLocation, PUE]

case class GetPartnerSocialActivityByDate(credentials: PUCC) extends PUM with PUI
case class GetPartnerSocialActivityByDateResponse(message: GetPartnerSocialActivityByDate, value: Either[PartnerUserException, PartnerSocialActivityByDate])
        extends PUM with MR[PartnerSocialActivityByDate, GetPartnerSocialActivityByDate, PUE]

case class GetProductSocialSummary(credentials: PUCC, productId: String) extends PUM with PUI
case class GetProductSocialSummaryResponse(message: GetProductSocialSummary, value: Either[PartnerUserException, ProductSocialSummary])
        extends PUM with MR[ProductSocialSummary, GetProductSocialSummary, PUE]

case class GetProductSocialActivityByDate(credentials: PUCC, productId: String) extends PUM with PUI
case class GetProductSocialActivityByDateResponse(message: GetProductSocialActivityByDate, value: Either[PartnerUserException, PartnerProductSocialActivityByDate])
        extends PUM with MR[PartnerProductSocialActivityByDate, GetProductSocialActivityByDate, PUE]

case class GetProducts(credentials: PUCC) extends PUM with PUI
case class GetProductsResponse(message: GetProducts, value: Either[PartnerUserException, PartnerProductsListView])
    extends PUM with MR[PartnerProductsListView, GetProducts, PUE]

case class GetTopProducts(credentials: PUCC) extends PUM with PUI
case class GetTopProductsResponse(message: GetTopProducts, value: Either[PartnerUserException, PartnerProductsListView])
    extends PUM with MR[PartnerProductsListView, GetTopProducts, PUE]

case class GetCustomers(credentials: PUCC) extends PUM with PUI
case class GetCustomersResponse(message: GetCustomers, value: Either[PartnerUserException,PartnerCustomerListView])
    extends PUM with MR[PartnerCustomerListView, GetCustomers, PUE]

case class GetEchoes(credentials: PUCC) extends PUM with PUI
case class GetEchoesResponse(message: GetEchoes,  value: Either[PartnerUserException, List[PartnerEchoView]])
    extends PUM with MR[List[PartnerEchoView], GetEchoes, PUE]

case class GetTopCustomers(credentials: PUCC) extends PUM with PUI
case class GetTopCustomersResponse(message: GetTopCustomers, value: Either[PartnerUserException,PartnerCustomerListView])
    extends PUM with MR[PartnerCustomerListView, GetTopCustomers, PUE]

case class GetComments(credentials: PUCC) extends PUM with PUI
case class GetCommentsResponse(message: GetComments, value: Either[PartnerUserException,List[FacebookComment]] )
    extends PUM with MR[List[FacebookComment], GetComments, PUE]

case class GetCommentsByProductId(credentials: PUCC, productId: String) extends PUM with PUI
case class GetCommentsByProductIdResponse(message: GetCommentsByProductId, value: Either[PartnerUserException,List[FacebookComment]])
    extends PUM with MR[List[FacebookComment], GetCommentsByProductId, PUE]


case class ActivatePartnerUser(credentials: PUCC, password: String) extends PUM with PUI
case class ActivatePartnerUserResponse(
        message: ActivatePartnerUser,
        value: Either[PUE, PartnerUser])
        extends PUM with MR[PartnerUser, ActivatePartnerUser, PUE]


case class InvalidPassword(
        _message: String = "Invalid password",
        _cause: Throwable = null,
        _code: Option[String] = Some("password.invalid")) extends PUE(_message, _cause, _code)

