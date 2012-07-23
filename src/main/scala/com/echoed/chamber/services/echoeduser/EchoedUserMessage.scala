package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.{MessageResponse => MR, EchoedException, Message}
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.domain.views.echoeduser.Profile
import com.echoed.chamber.domain._
import partner.PartnerSettings
import com.echoed.chamber.domain.views._



sealed trait EchoedUserMessage extends Message
sealed case class EchoedUserException(message: String = "", cause: Throwable = null) extends EchoedException(message,cause)

import com.echoed.chamber.services.echoeduser.{ EchoedUserMessage => EUM }
import com.echoed.chamber.services.echoeduser.{ EchoedUserException => EUE }


case class DuplicateEcho(
        echo: Echo,
        m: String = "",
        c: Throwable = null) extends EchoedUserException(m, c)

case class EmailAlreadyExists(
        email: String,
        m: String = "Email already in Use",
        c: Throwable = null) extends EchoedUserException(m, c)

abstract case class EchoedUserIdentifiable(echoedUserId: String) extends EUM

import com.echoed.chamber.services.echoeduser.{EchoedUserIdentifiable => EUI}

case class InitStory(
        _echoedUserId: String,
        storyId: Option[String] = None,
        echoId: Option[String] = None,
        partnerId: Option[String] = None) extends EUI(_echoedUserId)

case class InitStoryResponse(message: InitStory, value: Either[EUE, StoryInfo])
        extends EUM with MR[StoryInfo, InitStory, EUE]


case class CreateStory(
        _echoedUserId: String,
        title: String,
        imageId: String,
        partnerId: Option[String] = None,
        echoId: Option[String] = None,
        productInfo: Option[String] = None) extends EUI(_echoedUserId)

case class CreateStoryResponse(message: CreateStory, value: Either[EUE, Story])
        extends EUM with MR[Story, CreateStory, EUE]

case class TagStory(
        _echoedUserId: String,
        storyId: String,
        tagId: String) extends EUI(_echoedUserId)
case class TagStoryResponse(message: TagStory, value: Either[EUE, Story])
        extends EUM with MR[Story, TagStory, EUE]


case class UpdateStory(
        _echoedUserId: String,
        storyId: String,
        title: String,
        imageId: String) extends EUI(_echoedUserId)

case class UpdateStoryResponse(message: UpdateStory, value: Either[EUE, Story])
        extends EUM with MR[Story, UpdateStory, EUE]


case class CreateChapter(
        _echoedUserId: String,
        storyId: String,
        title: String,
        text: String,
        imageIds: Option[Array[String]]) extends EUI(_echoedUserId)

case class CreateChapterResponse(message: CreateChapter, value: Either[EUE, ChapterInfo])
        extends EUM with MR[ChapterInfo, CreateChapter, EUE]


case class UpdateChapter(
        _echoedUserId: String,
        chapterId: String,
        title: String,
        text: String,
        imageIds: Option[Array[String]] = None) extends EUI(_echoedUserId)

case class UpdateChapterResponse(message: UpdateChapter, value: Either[EUE, ChapterInfo])
        extends EUM with MR[ChapterInfo, UpdateChapter, EUE]


case class CreateComment(
        _echoedUserId: String,
        storyId: String,
        chapterId: String,
        text: String,
        parentCommentId: Option[String]) extends EUI(_echoedUserId)

case class CreateCommentResponse(message: CreateComment, value: Either[EUE, Comment])
        extends EUM with MR[Comment, CreateComment, EUE]

case class AssignFacebookService(facebookService: FacebookService) extends EUM
case class AssignFacebookServiceResponse(message: AssignFacebookService, value: Either[EUE, FacebookService])
    extends EUM with MR[FacebookService, AssignFacebookService, EUE]

case class AssignTwitterService(twitterService: TwitterService) extends EUM
case class AssignTwitterServiceResponse(message: AssignTwitterService, value: Either[EUE,TwitterService])
    extends EUM with MR[TwitterService,  AssignTwitterService, EUE]

case class EchoTo(
        echoedUserId: String,
        echoPossibilityId: String,
        facebookMessage: Option[String] = None,
        echoToFacebook: Boolean = false,
        twitterMessage: Option[String] = None,
        echoToTwitter: Boolean = false) extends EUM
case class EchoToResponse(message: EchoTo, value: Either[EUE, EchoFull]) extends EUM with MR[EchoFull, EchoTo, EUE]

case class EchoToFacebook(echo:Echo, echoMessage: Option[String]) extends EUM
case class EchoToFacebookResponse(message: EchoToFacebook, value: Either[EUE, FacebookPost])
    extends EUM with MR[FacebookPost, EchoToFacebook, EUE]

case class EchoToTwitter(echo:Echo, echoMessage: Option[String], partnerSettings: PartnerSettings) extends EUM
case class EchoToTwitterResponse(message:EchoToTwitter,  value: Either[EUE, TwitterStatus])
    extends EUM with MR[TwitterStatus, EchoToTwitter,  EUE]

case class PublishFacebookAction(action: String,  obj: String,  objUrl: String) extends EUM
case class PublishFacebookActionResponse(message: PublishFacebookAction, value: Either[EUE, Boolean])
    extends EUM with MR[Boolean, PublishFacebookAction, EUE]

case class GetFriendExhibit(echoedFriendUserId: String, page: Int) extends EUM
case class GetFriendExhibitResponse(message: GetFriendExhibit,  value: Either[EUE, FriendCloset])
    extends EUM with MR[FriendCloset, GetFriendExhibit, EUE]



case class GetEchoedUser() extends EUM
case class GetEchoedUserResponse(message: GetEchoedUser, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, GetEchoedUser,  EUE]

case class UpdateEchoedUserEmail(email: String) extends EUM
case class UpdateEchoedUserEmailResponse(message: UpdateEchoedUserEmail, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, UpdateEchoedUserEmail, EUE]

case class UpdateEchoedUser(echoedUser: EchoedUser) extends EUM
case class UpdateEchoedUserResponse(message: UpdateEchoedUser, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, UpdateEchoedUser, EUE]

case class GetProfile() extends EUM
case class GetProfileResponse(message: GetProfile, value: Either[EUE, Profile])
    extends EUM with MR[Profile, GetProfile, EUE]

case class GetExhibit(page: Int) extends EUM
case class GetExhibitResponse(message: GetExhibit, value: Either[EUE, ClosetPersonal])
    extends EUM with MR[ClosetPersonal, GetExhibit, EUE]

case class GetFeed(page: Int) extends EUM
case class GetFeedResponse(message: GetFeed, value: Either[EUE, Feed])
    extends EUM with MR[Feed, GetFeed, EUE]

case class GetEchoedFriends() extends EUM
case class GetEchoedFriendsResponse(message: GetEchoedFriends, value: Either[EUE, FriendFeed])
    extends EUM with MR[FriendFeed, GetEchoedFriends, EUE]

case class LocateWithId(echoedUserId: String) extends EUM
case class LocateWithIdResponse(message: LocateWithId, value: Either[EUE, EchoedUserService])
    extends EUM with MR[EchoedUserService, LocateWithId, EUE]

case class Logout(echoedUserId: String) extends EUM
case class LogoutResponse(message: Logout, value: Either[EUE, Boolean])
    extends EUM with MR[Boolean, Logout, EUE]

case class LocateWithFacebookService(facebookService: FacebookService) extends EUM
case class LocateWithFacebookServiceResponse(
    message: LocateWithFacebookService,
    value: Either[EUE, EchoedUserService])
    extends EUM with MR[EchoedUserService, LocateWithFacebookService, EUE]

case class LocateWithTwitterService(twitterService: TwitterService) extends EUM
case class LocateWithTwitterServiceResponse(message: LocateWithTwitterService,value: Either[EUE, EchoedUserService])
    extends EUM with MR[EchoedUserService, LocateWithTwitterService, EUE]

case class CreateEchoedUserServiceWithId(echoedUserId: String) extends EUM
case class CreateEchoedUserServiceWithIdResponse(message: CreateEchoedUserServiceWithId, value: Either[EUE, EchoedUserService])
    extends EUM with MR[EchoedUserService, CreateEchoedUserServiceWithId, EUE]

case class EchoedUserNotFound(id: String, m: String = "Echoed user not found") extends EUE(m)

case class CreateEchoedUserServiceWithFacebookService(facebookService: FacebookService) extends EUM
case class CreateEchoedUserServiceWithFacebookServiceResponse(message: CreateEchoedUserServiceWithFacebookService, value: Either[EUE, EchoedUserService])
    extends EUM with MR[EchoedUserService, CreateEchoedUserServiceWithFacebookService ,EUE]

case class CreateEchoedUserServiceWithTwitterService(twitterService: TwitterService) extends EUM
case class CreateEchoedUserServiceWithTwitterServiceResponse(message: CreateEchoedUserServiceWithTwitterService, value: Either[EUE, EchoedUserService])
    extends EUM with MR[EchoedUserService, CreateEchoedUserServiceWithTwitterService, EUE]


private[echoeduser] case class FetchFacebookFriends() extends EUM
private[echoeduser] case class FetchTwitterFollowers() extends EUM
