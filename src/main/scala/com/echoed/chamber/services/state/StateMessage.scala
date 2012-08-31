package com.echoed.chamber.services.state

import com.echoed.chamber.services.{MessageResponse => MR, Message}
import com.echoed.chamber.services.echoeduser.{EchoedUserServiceState, EchoedUserClientCredentials}
import com.echoed.chamber.domain.FacebookUser
import com.echoed.chamber.domain.AdminUser
import com.echoed.chamber.services.EchoedException
import com.echoed.chamber.domain.TwitterUser
import com.echoed.chamber.services.scheduler.Schedule
import com.echoed.chamber.domain.partner.PartnerUser
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials


private[services] sealed trait StateMessage extends Message
private[services] sealed case class StateException(message: String = "", cause: Throwable = null)
        extends EchoedException(message, cause)

import com.echoed.chamber.services.state.{StateMessage => SM}
import com.echoed.chamber.services.state.{StateException => SE}



private[services] case class EchoedUserNotFound(
        email: String,
        m: String = "Echoed user not found") extends SE(m)


private[services] case class FacebookUserNotFound(
        facebookUser: FacebookUser,
        m: String = "Facebook user not found") extends SE(m)


private[services] case class TwitterUserNotFound(
        twitterUser: TwitterUser,
        m: String = "Twitter user not found") extends SE(m)


private[services] abstract class ReadEchoedUserServiceState extends SM
import com.echoed.chamber.services.state.{ReadEchoedUserServiceState => REUSS}

private[services] case class ReadForEmail(email: String) extends REUSS
private[services] case class ReadForEmailResponse(
                message: ReadForEmail,
                value: Either[SE, EchoedUserServiceState])
                extends SM with MR[EchoedUserServiceState, ReadForEmail, SE]


private[services] case class ReadForCredentials(credentials: EchoedUserClientCredentials) extends REUSS
private[services] case class ReadForCredentialsResponse(
                message: ReadForCredentials,
                value: Either[SE, EchoedUserServiceState])
                extends SM with MR[EchoedUserServiceState, ReadForCredentials, SE]


private[services] case class ReadForFacebookUser(facebookUser: FacebookUser) extends REUSS
private[services] case class ReadForFacebookUserResponse(
                message: ReadForFacebookUser,
                value: Either[SE, EchoedUserServiceState])
                extends SM with MR[EchoedUserServiceState, ReadForFacebookUser, SE]


private[services] case class ReadForTwitterUser(twitterUser: TwitterUser) extends REUSS
private[services] case class ReadForTwitterUserResponse(
                message: ReadForTwitterUser,
                value: Either[SE, EchoedUserServiceState])
                extends SM with MR[EchoedUserServiceState, ReadForTwitterUser, SE]


private[services] case class ReadSchedulerServiceState() extends SM
private[services] case class ReadSchedulerServiceStateResponse(
                message: ReadSchedulerServiceState,
                value: Either[SE, Map[String, Schedule]])
                extends SM with MR[Map[String, Schedule], ReadSchedulerServiceState, SE]


private[services] case class PartnerUserNotFound(
        email: String,
        m: String = "Partner user not found") extends SE(m)

private[services] case class ReadPartnerUserForEmail(email: String) extends SM
private[services] case class ReadPartnerUserForEmailResponse(
                message: ReadPartnerUserForEmail,
                value: Either[SE, PartnerUser])
                extends SM with MR[PartnerUser, ReadPartnerUserForEmail, SE]


private[services] case class ReadPartnerUserForCredentials(credentials: PartnerUserClientCredentials) extends SM
private[services] case class ReadPartnerUserForCredentialsResponse(
                message: ReadPartnerUserForCredentials,
                value: Either[SE, PartnerUser])
                extends SM with MR[PartnerUser, ReadPartnerUserForCredentials, SE]


private[services] case class AdminUserNotFound(
        email: String,
        m: String = "Admin user not found") extends SE(m)

private[services] case class ReadAdminUserForEmail(email: String) extends SM
private[services] case class ReadAdminUserForEmailResponse(
                message: ReadAdminUserForEmail,
                value: Either[SE, AdminUser])
                extends SM with MR[AdminUser, ReadAdminUserForEmail, SE]


private[services] case class ReadAdminUserForCredentials(credentials: AdminUserClientCredentials) extends SM
private[services] case class ReadAdminUserForCredentialsResponse(
                message: ReadAdminUserForCredentials,
                value: Either[SE, AdminUser])
                extends SM with MR[AdminUser, ReadAdminUserForCredentials, SE]
