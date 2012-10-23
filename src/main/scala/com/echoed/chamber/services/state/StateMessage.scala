package com.echoed.chamber.services.state

import com.echoed.chamber.services.{MessageResponse => MR, Message}
import com.echoed.chamber.domain
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials
import com.echoed.chamber.services.echoeduser.EchoedUserServiceState
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.EchoedException
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import com.echoed.chamber.services.scheduler.Schedule


private[services] sealed trait StateMessage extends Message
private[services] sealed case class StateException(message: String = "", cause: Throwable = null)
        extends EchoedException(message, cause)

private[services] sealed case class NotFoundException(
        _message: String = "Not found",
        _cause: Throwable = null)
        extends StateException(_message, _cause)

import com.echoed.chamber.services.state.{StateMessage => SM}
import com.echoed.chamber.services.state.{StateException => SE}
import com.echoed.chamber.services.state.{NotFoundException => NFE}



private[services] case class EchoedUserNotFound(
        email: String,
        m: String = "Echoed user not found") extends NFE(m)


private[services] case class FacebookUserNotFound(
        facebookUser: domain.FacebookUser,
        m: String = "Facebook user not found") extends NFE(m)


private[services] case class TwitterUserNotFound(
        twitterUser: domain.TwitterUser,
        m: String = "Twitter user not found") extends NFE(m)


private[services] abstract class ReadEchoedUserServiceState extends SM
import com.echoed.chamber.services.state.{ReadEchoedUserServiceState => REUSS}


private[services] case class ReadForCredentials(credentials: EchoedUserClientCredentials) extends REUSS
private[services] case class ReadForCredentialsResponse(
                message: ReadForCredentials,
                value: Either[SE, EchoedUserServiceState])
                extends SM with MR[EchoedUserServiceState, ReadForCredentials, SE]


private[services] case class ReadForFacebookUser(facebookUser: domain.FacebookUser) extends REUSS
private[services] case class ReadForFacebookUserResponse(
                message: ReadForFacebookUser,
                value: Either[SE, EchoedUserServiceState])
                extends SM with MR[EchoedUserServiceState, ReadForFacebookUser, SE]


private[services] case class ReadForTwitterUser(twitterUser: domain.TwitterUser) extends REUSS
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
        m: String = "Partner user not found") extends NFE(m)

private[services] case class ReadPartnerUserForEmail(email: String) extends SM
private[services] case class ReadPartnerUserForEmailResponse(
                message: ReadPartnerUserForEmail,
                value: Either[SE, domain.partner.PartnerUser])
                extends SM with MR[domain.partner.PartnerUser, ReadPartnerUserForEmail, SE]


private[services] case class ReadPartnerUserForCredentials(credentials: PartnerUserClientCredentials) extends SM
private[services] case class ReadPartnerUserForCredentialsResponse(
                message: ReadPartnerUserForCredentials,
                value: Either[SE, domain.partner.PartnerUser])
                extends SM with MR[domain.partner.PartnerUser, ReadPartnerUserForCredentials, SE]


private[services] case class AdminUserNotFound(
        email: String,
        m: String = "Admin user not found") extends NFE(m)

private[services] case class ReadAdminUserForEmail(email: String) extends SM
private[services] case class ReadAdminUserForEmailResponse(
                message: ReadAdminUserForEmail,
                value: Either[SE, domain.AdminUser])
                extends SM with MR[domain.AdminUser, ReadAdminUserForEmail, SE]


private[services] case class ReadAdminUserForCredentials(credentials: AdminUserClientCredentials) extends SM
private[services] case class ReadAdminUserForCredentialsResponse(
                message: ReadAdminUserForCredentials,
                value: Either[SE, domain.AdminUser])
                extends SM with MR[domain.AdminUser, ReadAdminUserForCredentials, SE]


private[services] case class StoryNotFound(id: String, m: String = "Story not found") extends NFE(m)
private[services] case class StoryForEchoNotFound(s: domain.StoryState, m: String = "Story not found") extends NFE(m)


private[services] case class ReadStory(id: String) extends SM
private[services] case class ReadStoryResponse(
                message: ReadStory,
                value: Either[SE, domain.StoryState])
                extends SM with MR[domain.StoryState, ReadStory, SE]

private[services] case class ReadStoryForEcho(echoId: String, echoedUserId: String) extends SM
private[services] case class ReadStoryForEchoResponse(
                message: ReadStoryForEcho,
                value: Either[SE, domain.StoryState])
                extends SM with MR[domain.StoryState, ReadStoryForEcho, SE]