package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.services.facebook.{FacebookService}
import com.echoed.chamber.services.twitter.{TwitterService}
import com.echoed.chamber.domain.views.{EchoFull, Feed, Closet, ClosetPersonal, FriendCloset, PublicFeed}
import com.echoed.chamber.domain._


sealed trait EchoedUserMessage extends Message
sealed case class EchoedUserException(message: String = "", cause: Throwable = null) extends EchoedException(message,cause)

import com.echoed.chamber.services.echoeduser.{ EchoedUserMessage => EUM }
import com.echoed.chamber.services.echoeduser.{ EchoedUserException => EUE }


case class DuplicateEcho(
        echo: Echo,
        m: String = "",
        c: Throwable = null) extends EchoedUserException(m, c)

case class AssignFacebookService(facebookService: FacebookService) extends EUM
case class AssignFacebookServiceResponse(message: AssignFacebookService, value: Either[EUE, FacebookService])
    extends EUM with RM[FacebookService, AssignFacebookService, EUE]

case class AssignTwitterService(twitterService: TwitterService) extends EUM
case class AssignTwitterServiceResponse(message: AssignTwitterService, value: Either[EUE,TwitterService])
    extends EUM with RM[TwitterService,  AssignTwitterService, EUE]

case class EchoTo(
        echoedUserId: String,
        echoPossibilityId: String,
        facebookMessage: Option[String] = None,
        echoToFacebook: Boolean = false,
        twitterMessage: Option[String] = None,
        echoToTwitter: Boolean = false) extends EUM
case class EchoToResponse(message: EchoTo, value: Either[EUE, EchoFull]) extends EUM with RM[EchoFull, EchoTo, EUE]

case class EchoToFacebook(echo:Echo, echoMessage: Option[String]) extends EUM
case class EchoToFacebookResponse(message: EchoToFacebook, value: Either[EUE, FacebookPost])
    extends EUM with RM[FacebookPost, EchoToFacebook, EUE]

case class EchoToTwitter(echo:Echo, echoMessage: Option[String],partnerSettings: PartnerSettings) extends EUM
case class EchoToTwitterResponse(message:EchoToTwitter,  value: Either[EUE, TwitterStatus])
    extends EUM with RM[TwitterStatus, EchoToTwitter,  EUE]

case class PublishFacebookAction(action: String,  obj: String,  objUrl: String) extends EUM
case class PublishFacebookActionResponse(message: PublishFacebookAction, value: Either[EUE, FacebookAction])
    extends EUM with RM[FacebookAction, PublishFacebookAction, EUE]

case class GetFriendExhibit(echoedFriendUserId: String, page: Int) extends EUM
case class GetFriendExhibitResponse(message: GetFriendExhibit,  value: Either[EUE, FriendCloset])
    extends EUM with RM[FriendCloset, GetFriendExhibit, EUE]

case class GetEchoedUser() extends EUM
case class GetEchoedUserResponse(message: GetEchoedUser, value: Either[EUE, EchoedUser])
    extends EUM with RM[EchoedUser, GetEchoedUser,  EUE]

case class GetExhibit(page: Int) extends EUM
case class GetExhibitResponse(message: GetExhibit, value: Either[EUE, ClosetPersonal])
    extends EUM with RM[ClosetPersonal, GetExhibit, EUE]

case class GetPublicFeed(page: Int) extends EUM
case class GetPublicFeedResponse(message: GetPublicFeed, value: Either[EUE, PublicFeed])
    extends EUM with RM[PublicFeed, GetPublicFeed, EUE]

case class GetFeed(page: Int) extends EUM
case class GetFeedResponse(message: GetFeed, value: Either[EUE, Feed])
    extends EUM with RM[Feed, GetFeed, EUE]

case class GetPartnerFeed(partnerName: String, page: Int) extends EUM
case class GetPartnerFeedResponse(message: GetPartnerFeed,  value: Either[EUE, PublicFeed])
    extends EUM with RM[PublicFeed, GetPartnerFeed, EUE]

case class GetEchoedFriends() extends EUM
case class GetEchoedFriendsResponse(message: GetEchoedFriends, value: Either[EUE, List[EchoedFriend]])
    extends EUM with RM[List[EchoedFriend], GetEchoedFriends, EUE]

case class LocateWithId(echoedUserId: String) extends EUM
case class LocateWithIdResponse(message: LocateWithId, value: Either[EUE, EchoedUserService])
    extends EUM with RM[EchoedUserService, LocateWithId, EUE]

case class Logout(echoedUserId: String) extends EUM
case class LogoutResponse(message: Logout, value: Either[EUE, Boolean])
    extends EUM with RM[Boolean, Logout, EUE]

case class LocateWithFacebookService(facebookService: FacebookService) extends EUM
case class LocateWithFacebookServiceResponse(
    message: LocateWithFacebookService,
    value: Either[EUE, EchoedUserService])
    extends EUM with RM[EchoedUserService, LocateWithFacebookService, EUE]

case class LocateWithTwitterService(twitterService: TwitterService) extends EUM
case class LocateWithTwitterServiceResponse(message: LocateWithTwitterService,value: Either[EUE, EchoedUserService])
    extends EUM with RM[EchoedUserService, LocateWithTwitterService, EUE]

case class CreateEchoedUserServiceWithId(echoedUserId: String) extends EUM
case class CreateEchoedUserServiceWithIdResponse(message: CreateEchoedUserServiceWithId, value: Either[EUE, EchoedUserService])
    extends EUM with RM[EchoedUserService, CreateEchoedUserServiceWithId, EUE]

case class EchoedUserNotFound(id: String, m: String = "Echoed user not found") extends EUE(m)

case class CreateEchoedUserServiceWithFacebookService(facebookService: FacebookService) extends EUM
case class CreateEchoedUserServiceWithFacebookServiceResponse(message: CreateEchoedUserServiceWithFacebookService, value: Either[EUE, EchoedUserService])
    extends EUM with RM[EchoedUserService, CreateEchoedUserServiceWithFacebookService ,EUE]

case class CreateEchoedUserServiceWithTwitterService(twitterService: TwitterService) extends EUM
case class CreateEchoedUserServiceWithTwitterServiceResponse(message: CreateEchoedUserServiceWithTwitterService, value: Either[EUE, EchoedUserService])
    extends EUM with RM[EchoedUserService, CreateEchoedUserServiceWithTwitterService, EUE]


private[echoeduser] case class FetchFacebookFriends() extends EUM
private[echoeduser] case class FetchTwitterFollowers() extends EUM
