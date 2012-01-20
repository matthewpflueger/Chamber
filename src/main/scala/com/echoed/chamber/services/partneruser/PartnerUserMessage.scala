package com.echoed.chamber.services.partneruser

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.RetailerUser
import com.echoed.chamber.domain.views.{RetailerSocialSummary, ProductSocialSummary, RetailerProductsListView, RetailerCustomerListView}


sealed trait PartnerUserMessage extends Message
sealed case class PartnerUserException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

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
case class GetPartnerUserResponse(message: GetPartnerUser, value: Either[PartnerUserException, RetailerUser])
        extends PUM with RM[RetailerUser, GetPartnerUser, PUE]

case class GetRetailerSocialSummary() extends PUM
case class GetRetailerSocialSummaryResponse(message: GetRetailerSocialSummary, value: Either[PartnerUserException, RetailerSocialSummary])
        extends PUM with RM[RetailerSocialSummary, GetRetailerSocialSummary, PUE]

case class GetProductSocialSummary(productId: String) extends PUM
case class GetProductSocialSummaryResponse(message: GetProductSocialSummary, value: Either[PartnerUserException, ProductSocialSummary])
        extends PUM with RM[ProductSocialSummary, GetProductSocialSummary, PUE]

case class GetTopProducts() extends PUM
case class GetTopProductsResponse(message: GetTopProducts, value: Either[PartnerUserException, RetailerProductsListView])
    extends PUM with RM[RetailerProductsListView, GetTopProducts, PUE]

case class GetTopCustomers() extends PUM
case class GetTopCustomersResponse(message: GetTopCustomers, value: Either[PartnerUserException,RetailerCustomerListView])
    extends PUM with RM[RetailerCustomerListView, GetTopCustomers, PUE]

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



