package com.echoed.chamber.services.partner

import com.echoed.chamber.services.{MessageResponse => MR, OnlineOnlyMessage, EchoedClientCredentials, EchoedException, Message}
import com.echoed.chamber.domain.views._
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.partner.{PartnerSettings, PartnerUser, Partner}
import akka.actor.ActorRef
import com.echoed.chamber.services.echoeduser.{Follower, EchoedUserClientCredentials}
import context.PartnerContext
import org.springframework.validation.Errors
import java.util.Date
import com.echoed.chamber.domain.public.StoryPublic
import content.{ContentDescription, Content}


trait PartnerMessage extends Message
class PartnerException(
        val message: String = "",
        val cause: Throwable = null,
        val code: Option[String] = None,
        val arguments: Option[Array[AnyRef]] = None,
        val errors: Option[Errors] = None) extends EchoedException(message, cause, code, arguments, errors)

case class PartnerClientCredentials(partnerId: String) extends EchoedClientCredentials {
    val id = partnerId
}

trait PartnerIdentifiable {
    this: PartnerMessage =>
    def credentials: PartnerClientCredentials
    def partnerId = credentials.partnerId
}

sealed trait EchoIdentifiable {
    this: PartnerMessage =>
    def echoId: String
}

import com.echoed.chamber.services.partner.{PartnerMessage => PM}
import com.echoed.chamber.services.partner.{PartnerException => PE}
import com.echoed.chamber.services.partner.{PartnerClientCredentials => PCC}
import com.echoed.chamber.services.partner.{PartnerIdentifiable => PI}
import com.echoed.chamber.services.partner.{EchoIdentifiable => EI}


private[services] case class PartnerServiceState(
        partner: Partner,
        partnerSettings: PartnerSettings,
        partnerUser: Option[PartnerUser],
        followedByUsers: List[Follower],
        topics: List[Topic])

private[partner] case class RegisterPartnerService(partner: Partner)

private[partner] case class Create(msg: Locate, channel: ActorRef)


case class InvalidRegistration(_errors: Errors, m: String = "Error creating account") extends PE(m, errors = Some(_errors))
case class RegisterPartner(
        userName: String,
        email: String,
        siteName: String,
        siteUrl: String,
        shortName: String,
        community: String) extends PM
case class RegisterPartnerResponse(
        message: RegisterPartner,
        value: Either[PE, (PartnerUser, Partner)]) extends PM with MR[(PartnerUser, Partner), RegisterPartner, PE]


case class UpdatePartner(partner: Partner, partnerSettings: PartnerSettings, partnerUser: PartnerUser) extends PM
case class UpdatePartnerResponse(
        message: UpdatePartner, 
        value: Either[PE, Partner]) extends PM with MR[Partner, UpdatePartner, PE]

case class UpdatePartnerStory(
        credentials: PCC,
        story: StoryPublic) extends PM with PI with OnlineOnlyMessage
case class UpdatePartnerStoryResponse(
        message: UpdatePartnerStory,
        value: Either[PE, Boolean]) extends PM with MR[Boolean, UpdatePartnerStory, PE]

//private[services] case class GetPartnerSettings(credentials: PCC) extends PM with PI
//private[services] case class GetPartnerSettingsResponse(message: GetPartnerSettings, value: Either[PartnerException, List[PartnerSettings]])
//        extends PM with MR[List[PartnerSettings], GetPartnerSettings, PE]

private[services] case class InitializePartnerContent(credentials: PCC, content: List[Content]) extends PM
private[services] case class InitializePartnerContentResponse(
        message: InitializePartnerContent,
        value: Either[PE, Boolean]) extends PM with MR[Boolean, InitializePartnerContent, PE]

case class GetPartner() extends PM
case class GetPartnerResponse(
        message: GetPartner,
        value: Either[PE, Partner]) extends PM with MR[Partner, GetPartner, PE]

private[services] case class ReadAllPartnerContent(credentials: PCC) extends PM with PI
private[services] case class ReadAllPartnerContentResponse(
        message: ReadAllPartnerContent,
        value: Either[PE, List[Content]]) extends PM with MR[List[Content], ReadAllPartnerContent, PE]

case class RequestPartnerContent(credentials: PCC, page: Int, origin: String, c: ContentDescription) extends PM with PI
case class RequestPartnerContentResponse(
        message: RequestPartnerContent,
        value: Either[PE, Feed[PartnerContext]]) extends PM with MR[Feed[PartnerContext], RequestPartnerContent, PE]

case class RequestPartnerFollowers(credentials: PCC) extends PM with PI
case class RequestPartnerFollowersResponse(
        message: RequestPartnerFollowers,
        value: Either[PE, Feed[PartnerContext]]) extends PM with MR[Feed[PartnerContext], RequestPartnerFollowers, PE]

case class RequestTopics(credentials: PCC) extends PM with PI
case class RequestTopicsResponse(
        message: RequestTopics,
        value: Either[PE, List[Topic]]) extends PM with MR[List[Topic], RequestTopics, PE]

case class PutPartnerCustomization(
        credentials: PCC,
        customMap: Map[String,Any]) extends PM with PI
case class PutPartnerCustomizationResponse(
        message: PutPartnerCustomization,
        value: Either[PE, Map[String, Any]]) extends PM with MR[Map[String, Any], PutPartnerCustomization, PE]

case class PutTopic(
        credentials: PCC,
        title: String,
        description: Option[String] = None,
        beginOn: Option[Date] = None,
        endOn: Option[Date] = None,
        topicId: Option[String] = None,
        community: Option[String] = None) extends PM with PI
case class PutTopicResponse(
        message: PutTopic,
        value: Either[PE, Topic]) extends PM with MR[Topic, PutTopic, PE]

case class InvalidDateRange(
        m: String = "Invalid date range",
        c: Option[String] = Some("date.range.invalid")) extends PartnerException(m, code = c)

case class FetchPartner(credentials: PCC) extends PM with PI
case class FetchPartnerResponse(
        message: FetchPartner,
        value: Either[PE, Partner]) extends PM with MR[Partner, FetchPartner, PE]

case class PartnerAndPartnerSettings(partner: Partner, partnerSettings: PartnerSettings, customization: Map[String, Any])
case class FetchPartnerAndPartnerSettings(credentials: PCC) extends PM with PI
case class FetchPartnerAndPartnerSettingsResponse(
        message: FetchPartnerAndPartnerSettings,
        value: Either[PE, PartnerAndPartnerSettings]) extends PM with MR[PartnerAndPartnerSettings, FetchPartnerAndPartnerSettings, PE]


private[services] case class AddPartnerFollower(credentials: PCC, echoedUser: EchoedUser) extends PM with PI
private[services] case class AddPartnerFollowerResponse(
        message: AddPartnerFollower,
        value: Either[PE, Partner]) extends PM with MR[Partner, AddPartnerFollower, PE]

private[services] case class RemovePartnerFollower(credentials: PCC, echoedUser: EchoedUser) extends PM with PI
private[services] case class RemovePartnerFollowerResponse(
        message: RemovePartnerFollower,
        value: Either[PE, Partner]) extends PM with MR[Partner, RemovePartnerFollower, PE]


private[services] case class PartnerFollowerNotification(category: String, value: Map[String, String])

private[services] case class NotifyPartnerFollowers(
        credentials: PCC,
        echoedUserClientCredentials: EchoedUserClientCredentials,
        notification: Notification) extends PM with PI


case class Locate(partnerId: String) extends PM
case class LocateResponse(
        message: Locate,
        value: Either[PE, ActorRef]) extends PM with MR[ActorRef, Locate, PE]


case class LocateByEchoId(echoId: String) extends PM
case class LocateByEchoIdResponse(
        message: LocateByEchoId,
        value: Either[PE, ActorRef]) extends PM with MR[ActorRef, LocateByEchoId, PE]


case class LocateByDomain(domain: String, context: Option[AnyRef] = None) extends PM
case class LocateByDomainResponse(
        message: LocateByDomain,
        value: Either[PE, ActorRef]) extends PM with MR[ActorRef, LocateByDomain, PE]


case class ViewDescription(view: String, model: Map[String, Any])


case class GetView(credentials: PCC) extends PM with PI
case class GetViewResponse(
        message: GetView,
        value: Either[PE, ViewDescription]) extends PM with MR[ViewDescription, GetView, PE]



case class PartnerNotFound(
        partnerId: String,
        _message: String = "Partner not found",
        _cause: Throwable = null,
        _code: Option[String] = Some("notfound.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args) {
    def this(partnerId: String) = this(partnerId, _args = Some(Array(partnerId)))
}


case class PartnerAlreadyExists(
        partnerService: ActorRef,
        _message: String = "Partner already exists",
        _cause: Throwable = null,
        _code: Option[String] = Some("alreadyexists.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args)

case class PartnerNotActive(
        partner: Partner,
        _message: String = "Partner %s (%s) is not active",
        _cause: Throwable = null,
        _code: Option[String] = Some("notfound.partner"),
        _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args) {
    def this(partner: Partner) = this(partner, _args = Some(Array(partner.id, partner.name)))
    override def getMessage = _message format(partner.name, partner.id)
}


case class InvalidEchoRequest(_message: String = "Invalid echo request",
                              _cause: Throwable = null,
                              _code: Option[String] = Some("invalid.echoRequest"),
                              _args: Option[Array[AnyRef]] = None) extends PE(_message, _cause, _code, _args)

private[partner] case class FilteredException(
        _message: String,
        echoClick: EchoClick) extends PE(_message)


case class RecordEchoClick(echoId: String, echoClick: EchoClick) extends PM with EI
case class RecordEchoClickResponse(message: RecordEchoClick, value: Either[PE, EchoPossibilityView])
        extends PM with MR[EchoPossibilityView, RecordEchoClick, PE]



case class RequestStory(credentials: PCC, topicId: Option[String] = None) extends PM with PI
case class RequestStoryResponseEnvelope(
        partner: Partner,
        partnerSettings: PartnerSettings,
        topic: Option[Topic] = None)
case class RequestStoryResponse(message: RequestStory, value: Either[PE, RequestStoryResponseEnvelope])
        extends PM with MR[RequestStoryResponseEnvelope, RequestStory, PE]


case class RequestEcho(
        credentials: PCC,
        request: String,
        browserId: String,
        ipAddress: String,
        userAgent: String,
        referrerUrl: String,
        echoedUserId: Option[String] = None,
        echoClickId: Option[String] = None,
        view: Option[String] = None) extends PM with PI
case class RequestEchoResponse(
        message: RequestEcho,
        value: Either[PE, EchoPossibilityView]) extends PM with MR[EchoPossibilityView, RequestEcho, PE]


case class GetEcho(echoId: String) extends PM with EI
case class GetEchoResponse(
        message: GetEcho,
        value: Either[PE, EchoPossibilityView]) extends PM with MR[EchoPossibilityView, GetEcho, PE]


case class RecordEchoStep(
        echoId: String,
        step: String,
        echoedUserId: Option[String] = None,
        echoClickId: Option[String] = None) extends PM with EI
case class RecordEchoStepResponse(
        message: RecordEchoStep,
        value: Either[PE, EchoPossibilityView]) extends PM with MR[EchoPossibilityView, RecordEchoStep, PE]


case class EchoExists(
        echoPossibilityView: EchoPossibilityView,
        _message: String = "Item already echoed",
        _cause: Throwable = null) extends PE(_message, _cause)

case class EchoNotFound(id: String, m: String = "Echo not found %s") extends PE(m format id)



