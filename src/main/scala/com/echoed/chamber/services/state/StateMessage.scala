package com.echoed.chamber.services.state

import com.echoed.chamber.services.{MessageResponse => MR, Message}
import com.echoed.chamber.services.echoeduser.{EchoedUserServiceState, EchoedUserClientCredentials}
import com.echoed.chamber.domain.FacebookUser
import com.echoed.chamber.domain.AdminUser
import com.echoed.chamber.services.EchoedException
import com.echoed.chamber.domain.TwitterUser


private[services] sealed trait StateMessage extends Message
private[services] sealed case class StateException(message: String = "", cause: Throwable = null)
        extends EchoedException(message, cause)

import com.echoed.chamber.services.state.{StateMessage => SM}
import com.echoed.chamber.services.state.{StateException => SE}


private[services] case class ReadAdminUserServiceManagerState() extends SM
private[services] case class ReadAdminUserServiceManagerStateResponse(
        message: ReadAdminUserServiceManagerState,
        value: Either[SE, Map[String, String]])
        extends SM with MR[Map[String, String], ReadAdminUserServiceManagerState, SE]

private[services] case class ReadAdminUserServiceState(adminUserId: String) extends SM
private[services] case class ReadAdminUserServiceStateResponse(
        message: ReadAdminUserServiceState,
        value: Either[SE, AdminUser])
        extends SM with MR[AdminUser, ReadAdminUserServiceState, SE]



private[services] case class ReadPartnerUserServiceManagerState() extends SM
private[services] case class ReadPartnerUserServiceManagerStateResponse(
        message: ReadPartnerUserServiceManagerState,
        value: Either[SE, Map[String, String]])
        extends SM with MR[Map[String, String], ReadPartnerUserServiceManagerState, SE]


private[services] case class FacebookUserNotFound(
        facebookUser: FacebookUser,
        m: String = "Facebook user not found") extends SE(m)

private[services] case class TwitterUserNotFound(
        twitterUser: TwitterUser,
        m: String = "Twitter user not found") extends SE(m)


private[services] abstract class ReadEchoedUserServiceState extends SM

import com.echoed.chamber.services.state.{ReadEchoedUserServiceState => REUSS}

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

