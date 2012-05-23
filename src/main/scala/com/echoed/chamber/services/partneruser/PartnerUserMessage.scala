package com.echoed.chamber.services.partneruser

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.views.{PartnerSocialSummary, ProductSocialSummary, PartnerProductsListView, PartnerCustomerListView, PartnerProductSocialActivityByDate, PartnerSocialActivityByDate,CustomerSocialSummary,PartnerCustomerSocialActivityByDate, PartnerEchoView}
import com.echoed.chamber.domain._


sealed trait PartnerUserMessage extends Message
sealed case class PartnerUserException(
        message: String = "",
        cause: Throwable = null,
        code: Option[String] = None) extends EchoedException(message, cause, code)

import com.echoed.chamber.services.partneruser.{PartnerUserMessage => PUM}
import com.echoed.chamber.services.partneruser.{PartnerUserException => PUE}


case class Login(email: String, password: String) extends PUM
case class LoginError(m: String = "", c: Throwable = null) extends PUE(m, c)
case class LoginResponse(message: Login, value: Either[LoginError, PartnerUserService])
        extends PUM with RM[PartnerUserService, Login, LoginError]

case class Logout(partnerUserId: String) extends PUM
case class LogoutResponse(message: Logout, value: Either[PUE, Boolean])
    extends PUM with RM[Boolean, Logout, PUE]

case class GetPartnerUser() extends PUM
case class GetPartnerUserResponse(message: GetPartnerUser, value: Either[PartnerUserException, PartnerUser])
        extends PUM with RM[PartnerUser, GetPartnerUser, PUE]

case class GetPartnerSettings() extends PUM
case class GetPartnerSettingsResponse(message: GetPartnerSettings, value: Either[PartnerUserException, List[PartnerSettings]])
        extends PUM with RM[List[PartnerSettings], GetPartnerSettings, PUE]

case class GetCustomerSocialSummary(echoedUserId: String) extends PUM
case class GetCustomerSocialSummaryResponse(message: GetCustomerSocialSummary, value: Either[PartnerUserException, CustomerSocialSummary])
        extends PUM with RM[CustomerSocialSummary, GetCustomerSocialSummary, PUE]

case class GetCustomerSocialActivityByDate(echoedUserId: String) extends PUM
case class GetCustomerSocialActivityByDateResponse(message: GetCustomerSocialActivityByDate, value: Either[PartnerUserException, PartnerCustomerSocialActivityByDate])
        extends PUM with RM[PartnerCustomerSocialActivityByDate, GetCustomerSocialActivityByDate, PUE]


case class GetPartnerSocialSummary() extends PUM
case class GetPartnerSocialSummaryResponse(message: GetPartnerSocialSummary, value: Either[PartnerUserException, PartnerSocialSummary])
        extends PUM with RM[PartnerSocialSummary, GetPartnerSocialSummary, PUE]

case class GetEchoClickGeoLocation() extends PUM
case class GetEchoClickGeoLocationResponse(message: GetEchoClickGeoLocation, value: Either[PartnerUserException, List[GeoLocation]])
        extends PUM with RM[List[GeoLocation], GetEchoClickGeoLocation, PUE]

case class GetPartnerSocialActivityByDate() extends PUM
case class GetPartnerSocialActivityByDateResponse(message: GetPartnerSocialActivityByDate, value: Either[PartnerUserException, PartnerSocialActivityByDate])
        extends PUM with RM[PartnerSocialActivityByDate, GetPartnerSocialActivityByDate, PUE]

case class GetProductSocialSummary(productId: String) extends PUM
case class GetProductSocialSummaryResponse(message: GetProductSocialSummary, value: Either[PartnerUserException, ProductSocialSummary])
        extends PUM with RM[ProductSocialSummary, GetProductSocialSummary, PUE]

case class GetProductSocialActivityByDate(productId: String) extends PUM
case class GetProductSocialActivityByDateResponse(message: GetProductSocialActivityByDate, value: Either[PartnerUserException, PartnerProductSocialActivityByDate])
        extends PUM with RM[PartnerProductSocialActivityByDate, GetProductSocialActivityByDate, PUE]

case class GetProducts() extends PUM
case class GetProductsResponse(message: GetProducts, value: Either[PartnerUserException, PartnerProductsListView])
    extends PUM with RM[PartnerProductsListView, GetProducts, PUE]

case class GetTopProducts() extends PUM
case class GetTopProductsResponse(message: GetTopProducts, value: Either[PartnerUserException, PartnerProductsListView])
    extends PUM with RM[PartnerProductsListView, GetTopProducts, PUE]

case class GetCustomers() extends PUM
case class GetCustomersResponse(message: GetCustomers, value: Either[PartnerUserException,PartnerCustomerListView])
    extends PUM with RM[PartnerCustomerListView, GetCustomers, PUE]

case class GetEchoes() extends PUM
case class GetEchoesResponse(message: GetEchoes,  value: Either[PartnerUserException, List[PartnerEchoView]])
    extends PUM with RM[List[PartnerEchoView], GetEchoes, PUE]

case class GetTopCustomers() extends PUM
case class GetTopCustomersResponse(message: GetTopCustomers, value: Either[PartnerUserException,PartnerCustomerListView])
    extends PUM with RM[PartnerCustomerListView, GetTopCustomers, PUE]

case class GetComments() extends PUM
case class GetCommentsResponse(message: GetComments, value: Either[PartnerUserException,List[FacebookComment]] )
    extends PUM with RM[List[FacebookComment], GetComments, PUE]

case class GetCommentsByProductId(productId: String) extends PUM
case class GetCommentsByProductIdResponse(message: GetCommentsByProductId, value: Either[PartnerUserException,List[FacebookComment]])
    extends PUM with RM[List[FacebookComment], GetCommentsByProductId, PUE]

case class CreatePartnerUserService(email: String) extends PUM
case class CreatePartnerUserServiceResponse(
        message: CreatePartnerUserService,
        value: Either[PUE, PartnerUserService])
        extends PUM with RM[PartnerUserService, CreatePartnerUserService, PUE]

case class Locate(partnerUserId: String) extends PUM
case class LocateResponse(
        message: Locate,
        value: Either[PUE, PartnerUserService])
        extends PUM with RM[PartnerUserService, Locate, PUE]

case class ActivatePartnerUser(password: String) extends PUM
case class ActivatePartnerUserResponse(
        message: ActivatePartnerUser,
        value: Either[PUE, PartnerUser])
        extends PUM with RM[PartnerUser, ActivatePartnerUser, PUE]

case class InvalidPassword(
        _message: String = "Invalid password",
        _cause: Throwable = null,
        _code: Option[String] = Some("password.invalid")) extends PUE(_message, _cause, _code)


