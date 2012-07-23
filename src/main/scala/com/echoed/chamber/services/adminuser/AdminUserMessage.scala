package com.echoed.chamber.services.adminuser

import com.echoed.chamber.services.{MessageResponse => MR, Event, EchoedClientCredentials, EchoedException, Message}

import java.util.{List => JList}
import com.echoed.chamber.domain._
import partner.{PartnerSettings, Partner}
import akka.actor.ActorRef

sealed trait AdminUserMessage extends Message
sealed case class AdminUserException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

trait AdminUserClientCredentials {
    this: EchoedClientCredentials =>
    def adminUserId = id
}

trait AdminUserIdentifiable {
    this: AdminUserMessage =>
    def credentials: AdminUserClientCredentials
    def adminUserId = credentials.adminUserId
}

import com.echoed.chamber.services.adminuser.{AdminUserMessage => AUM}
import com.echoed.chamber.services.adminuser.{AdminUserException => AUE}
import com.echoed.chamber.services.adminuser.{AdminUserClientCredentials => AUCC}
import com.echoed.chamber.services.adminuser.{AdminUserIdentifiable => AUI}

private[adminuser] case class CreateAdminUserService(msg: CreateAdminUser, sender: ActorRef)

case class CreateAdminUser(credentials: AUCC, adminUser: AdminUser) extends AUM with AUI
case class CreateAdminUserResponse(
                message: CreateAdminUser,
                value: Either[AUE,  AdminUser])
                extends AUM with MR[AdminUser, CreateAdminUser, AUE]


//case class CreateAdminUserService(email: String) extends AUM
//case class CreateAdminUserServiceResponse(
//                message: CreateAdminUserService,
//                value: Either[AUE, ActorRef])
//                extends AUM with MR[ActorRef, CreateAdminUserService, AUE]

case class GetAdminUser(credentials: AUCC) extends AUM with AUI
case class GetAdminUserResponse(
                message: GetAdminUser,
                value: Either[AUE,AdminUser])
                extends AUM with MR[AdminUser,GetAdminUser,AUE]

case class GetPartners(credentials: AUCC) extends AUM with AUI
case class GetPartnersResponse(
                message: GetPartners,
                value: Either[AUE, JList[Partner]])
                extends AUM with MR[JList[Partner], GetPartners, AUE]

case class GetPartnerSettings(credentials: AUCC, partnerId: String) extends AUM with AUI
case class GetPartnerSettingsResponse(
                message: GetPartnerSettings, 
                value: Either[AUE, JList[PartnerSettings]])
                extends AUM with MR[JList[PartnerSettings], GetPartnerSettings, AUE]

case class GetCurrentPartnerSettings(credentials: AUCC, partnerId: String) extends AUM with AUI
case class GetCurrentPartnerSettingsResponse(
                message: GetCurrentPartnerSettings,
                value: Either[AUE, PartnerSettings])
                extends AUM with MR[PartnerSettings, GetCurrentPartnerSettings, AUE]

case class GetPartner(credentials: AUCC, partnerId: String) extends AUM with AUI
case class GetPartnerResponse(
                message: GetPartner,
                value: Either[AUE, Partner])
                extends AUM with MR[Partner, GetPartner, AUE]

case class GetUsers(credentials: AUCC) extends AUM with AUI
case class GetUsersResponse(
                message: GetUsers, 
                value: Either[AUE,JList[EchoedUser]])
                extends AUM with MR[JList[EchoedUser], GetUsers, AUE]

case class GetEchoPossibilities(credentials: AUCC) extends AUM with AUI
case class GetEchoPossibilitiesResponse(
                message: GetEchoPossibilities,
                value: Either[AUE, JList[Echo]])
                extends AUM with MR[JList[Echo], GetEchoPossibilities, AUE]

//case class LocateAdminUserService(email: String) extends AUM
//case class LocateAdminUserServiceResponse(
//                message: LocateAdminUserService,
//                value: Either[AUE, ActorRef])
//                extends AUM with MR[ActorRef, LocateAdminUserService, AUE]

case class Login(email:String, password: String) extends AUM
case class LoginError(m: String = "", c: Throwable = null) extends AUE(m, c)
case class LoginResponse(
                message: Login, 
                value: Either[AUE, AdminUser])
                extends AUM with MR[AdminUser, Login, AUE]

case class Logout(credentials: AUCC) extends AUM with AUI
case class LogoutResponse(message: Logout, value: Either[AUE, Boolean])
    extends AUM with MR[Boolean, Logout, AUE]

case class UpdatePartnerSettings(
        credentials: AUCC,
        partnerSettings: PartnerSettings) extends AUM with AUI
case class UpdatePartnerSettingsResponse(message: UpdatePartnerSettings, value: Either[AUE, PartnerSettings])
    extends AUM with MR[PartnerSettings, UpdatePartnerSettings, AUE]

case class UpdatePartnerHandleAndCategory(
        credentials: AUCC,
        partnerId: String,
        partnerHandle: String,
        partnerCategory: String) extends AUM with AUI
case class UpdatePartnerHandleAndCategoryResponse(message: UpdatePartnerHandleAndCategory, value: Either[AUE, String])
    extends AUM with MR[String, UpdatePartnerHandleAndCategory, AUE]