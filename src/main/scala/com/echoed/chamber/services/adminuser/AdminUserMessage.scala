package com.echoed.chamber.services.adminuser

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.AdminUser
import com.echoed.chamber.domain.EchoedUser

import java.util.{List => JList}

sealed trait AdminUserMessage extends Message
sealed case class AdminUserException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.adminuser.{AdminUserMessage => AUM}
import com.echoed.chamber.services.adminuser.{AdminUserException => AUE}

case class CreateAdminUser(email:String, name:String,  password: String) extends AUM
case class CreateAdminUserResponse(
                message: CreateAdminUser,
                value: Either[AUE,  AdminUser])
                extends AUM with RM[AdminUser, CreateAdminUser, AUE]

case class CreateAdminUserService(email: String) extends AUM
case class CreateAdminUserServiceResponse(
                message: CreateAdminUserService,
                value: Either[AUE, AdminUserService])
                extends AUM with RM[AdminUserService, CreateAdminUserService, AUE]

case class GetAdminUser() extends AUM
case class GetAdminUserResponse(
                message: GetAdminUser,
                value: Either[AUE,AdminUser])
                extends AUM with RM[AdminUser,GetAdminUser,AUE]

case class GetUsers() extends AUM
case class GetUsersResponse(
                message: GetUsers, 
                value: Either[AUE,JList[EchoedUser]])
                extends AUM with RM[JList[EchoedUser], GetUsers, AUE]

case class LocateAdminUserService(email: String) extends AUM
case class LocateAdminUserServiceResponse(
                message: LocateAdminUserService,
                value: Either[AUE, AdminUserService])
                extends AUM with RM[AdminUserService, LocateAdminUserService, AUE]

case class Login(email:String,password: String) extends AUM
case class LoginError(m: String = "", c: Throwable = null) extends AUE(m, c)
case class LoginResponse(
                message: Login, 
                value: Either[AUE, AdminUserService])
                extends AUM with RM[AdminUserService,Login,AUE]

case class Logout(partnerUserId: String) extends AUM
case class LogoutResponse(message: Logout, value: Either[AUE, Boolean])
    extends AUM with RM[Boolean, Logout, AUE]