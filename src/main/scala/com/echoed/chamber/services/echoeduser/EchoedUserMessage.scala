package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.{MessageResponse => MR, Correlated, EchoedClientCredentials, EchoedException, Message}
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.views._
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.{FacebookAccessToken, FacebookCode}
import scala.collection.immutable.Stack
import org.springframework.validation.Errors


sealed trait EchoedUserMessage extends Message
sealed class EchoedUserException(
        val message: String = "",
        val cause: Throwable = null,
        val code: Option[String] = None,
        val arguments: Option[Array[AnyRef]] = None,
        val errors: Option[Errors] = None) extends EchoedException(message, cause, code, arguments, errors)

case class EchoedUserClientCredentials(
        id: String,
        name: Option[String] = None,
        email: Option[String] = None,
        screenName: Option[String] = None,
        facebookId: Option[String] = None,
        twitterId: Option[String] = None,
        password: Option[String] = None) extends EchoedClientCredentials {

    def isComplete = id != null && email.isDefined && screenName.isDefined && password.isDefined
}



trait EchoedUserIdentifiable {
    this: EchoedUserMessage =>
    def credentials: EchoedUserClientCredentials
    def id = credentials.id
}


import com.echoed.chamber.services.echoeduser.{EchoedUserMessage => EUM}
import com.echoed.chamber.services.echoeduser.{EchoedUserException => EUE}
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials => EUCC}
import com.echoed.chamber.services.echoeduser.{EchoedUserIdentifiable => EUI}


private[services] case class EchoedUserServiceState(
        echoedUser: EchoedUser,
        echoedUserSettings: EchoedUserSettings,
        facebookUser: Option[FacebookUser],
        twitterUser: Option[TwitterUser],
        notifications: Stack[Notification],
        followingUsers: List[Follower],
        followedByUsers: List[Follower],
        followingPartners: List[PartnerFollower])


case class DuplicateEcho(
        echo: Echo,
        m: String = "",
        c: Throwable = null) extends EchoedUserException(m, c)

case class EmailAlreadyExists(
        email: String,
        m: String = "Email already in Use",
        c: Throwable = null) extends EchoedUserException(m, c)


case class AddFacebook(
        credentials: EUCC,
        code: String,
        queryString: String) extends EUM with EUI

private[echoeduser] case class LoginWithFacebookUser(
        facebookUser: FacebookUser,
        correlation: LoginWithFacebook,
        override val correlationSender: Option[ActorRef]) extends EUM with Correlated[LoginWithFacebook]


case class LoginWithFacebook(loginInfo: Either[FacebookCode, FacebookAccessToken]) extends EUM
case class LoginWithFacebookResponse(message: LoginWithFacebook, value: Either[EUE, EUCC])
        extends EUM with MR[EUCC, LoginWithFacebook, EUE]


case class AddTwitter(
        credentials: EUCC,
        oAuthToken: String,
        oAuthVerifier: String) extends EUM with EUI

private[echoeduser] case class LoginWithTwitterUser(
        twitterUser: TwitterUser,
        correlation: LoginWithTwitter,
        override val correlationSender: Option[ActorRef]) extends EUM with Correlated[LoginWithTwitter]

case class LoginWithTwitter(oAuthToken: String, oAuthVerifier: String) extends EUM
case class LoginWithTwitterResponse(message: LoginWithTwitter, value: Either[EUE, EUCC])
        extends EUM with MR[EUCC, LoginWithTwitter, EUE]

case class GetTwitterAuthenticationUrl(callbackUrl: String) extends EUM
case class GetTwitterAuthenticationUrlResponse(message: GetTwitterAuthenticationUrl, value: Either[EUE, String])
        extends EUM with MR[String, GetTwitterAuthenticationUrl, EUE]


private[echoeduser] case class LoginWithCredentials(
        credentials: EUCC,
        correlation: EUM,
        override val correlationSender: Option[ActorRef]) extends EUM with EUI with Correlated[EUM]


case class FollowUser(credentials: EUCC, userToFollowerId: String) extends EUM with EUI
case class FollowUserResponse(message: FollowUser, value: Either[EUE, List[Follower]])
        extends EUM with MR[List[Follower], FollowUser, EUE]

case class FollowPartner(credentials: EUCC, partnerId: String) extends EUM with EUI
case class FollowPartnerResponse(message: FollowPartner, value: Either[EUE, Boolean])
        extends EUM with MR[Boolean, FollowPartner, EUE]

private[echoeduser] case class AddFollower(credentials: EUCC, echoedUser: EchoedUser)extends EUM with EUI
private[echoeduser] case class AddFollowerResponse(message: AddFollower, value: Either[EUE, EchoedUser])
        extends EUM with MR[EchoedUser, AddFollower, EUE]

case class UnFollowUser(credentials: EUCC, followingUserId: String) extends EUM with EUI
case class UnFollowUserResponse(message: UnFollowUser, value: Either[EUE, List[Follower]])
        extends EUM with MR[List[Follower], UnFollowUser, EUE]

private[echoeduser] case class RemoveFollower(credentials: EUCC, echoedUser: EchoedUser) extends EUM with EUI
private[echoeduser] case class RemoveFollowerResponse(message: RemoveFollower, value: Either[EUE, EchoedUser])
        extends EUM with MR[EchoedUser, RemoveFollower, EUE]

case class PartnerFollower(partnerId: String, name: String, handle: String)

case class Follower(echoedUserId: String, name: String, screenName: String, facebookId: String, twitterId: String)
object Follower {
    def apply(eu: EchoedUser): Follower = Follower(eu.id, eu.name, eu.screenName, eu.facebookId, eu.twitterId)
}


private[echoeduser] case class NotifyFollowers(credentials: EUCC, notification: Notification) extends EUM with EUI

case class ListFollowingUsers(credentials: EUCC) extends EUM with EUI
case class ListFollowingUsersResponse(message: ListFollowingUsers, value: Either[EUE, List[Follower]])
        extends EUM with MR[List[Follower], ListFollowingUsers, EUE]

case class ListFollowedByUsers(credentials: EUCC) extends EUM with EUI
case class ListFollowedByUsersResponse(message: ListFollowedByUsers, value: Either[EUE, List[Follower]])
        extends EUM with MR[List[Follower], ListFollowedByUsers, EUE]

case class ListFollowingPartners(credentials: EUCC) extends EUM with EUI
case class ListFollowingPartnersResponse(message: ListFollowingPartners, value: Either[EUE, List[PartnerFollower]])
        extends EUM with MR[List[PartnerFollower], ListFollowingPartners, EUE]

case class FetchNotifications(credentials: EUCC) extends EUM with EUI
case class FetchNotificationsResponse(message: FetchNotifications, value: Either[EUE, Stack[Notification]])
        extends EUM with MR[Stack[Notification], FetchNotifications, EUE]


case class MarkNotificationsAsRead(credentials: EUCC, ids: Set[String]) extends EUM with EUI
case class MarkNotificationsAsReadResponse(message: MarkNotificationsAsRead, value: Either[EUE, Boolean])
        extends EUM with MR[Boolean, MarkNotificationsAsRead, EUE]


case class ReadSettings(credentials: EUCC) extends EUM with EUI
case class ReadSettingsResponse(message: ReadSettings, value: Either[EUE, EchoedUserSettings])
        extends EUM with MR[EchoedUserSettings, ReadSettings, EUE]


case class NewSettings(credentials: EUCC, settings: Map[String, AnyRef]) extends EUM with EUI
case class NewSettingsResponse(message: NewSettings, value: Either[EUE,  EchoedUserSettings])
        extends EUM with MR[EchoedUserSettings, NewSettings, EUE]


case class RegisterNotification(credentials: EUCC, notification: Notification) extends EUM with EUI

case class EmailNotifications(credentials: EUCC, notificationType: Option[String] = None) extends EUM with EUI

private[echoeduser] case class RegisterStory(story: Story)

sealed trait StoryIdentifiable {
    this: EchoedUserMessage =>

    def storyId: String
}

import com.echoed.chamber.services.echoeduser.{StoryIdentifiable => SI}

case class InitStory(
        credentials: EUCC,
        storyId: Option[String] = None,
        echoId: Option[String] = None,
        partnerId: Option[String] = None,
        topicId: Option[String] = None) extends EUM with EUI

case class InitStoryResponse(message: InitStory, value: Either[EUE, StoryInfo])
        extends EUM with MR[StoryInfo, InitStory, EUE]

case class VoteStory(
        credentials: EUCC,
        storyOwnerId: String,
        storyId: String,
        value: Int) extends EUM with EUI with SI
case class VoteStoryResponse(message: VoteStory, value: Either[EUE, Map[String, Vote]])
        extends EUM with MR[Map[String, Vote],  VoteStory, EUE]

case class NewVote(
        credentials: EUCC,
        byEchoedUser: EchoedUser,
        storyId: String,
        value: Int) extends EUM with EUI with SI

case class NewVoteResponse(message: NewVote, value: Either[EUE, Map[String, Vote]])
        extends EUM with MR[Map[String, Vote], NewVote, EUE]


case class CreateStory(
        credentials: EUCC,
        storyId: String,
        title: String,
        imageId: Option[String] = None,
        partnerId: Option[String] = None,
        productInfo: Option[String] = None,
        community: Option[String] = None,
        echoId: Option[String] = None,
        topicId: Option[String] = None  ) extends EUM with EUI with SI

case class CreateStoryResponse(message: CreateStory, value: Either[EUE, Story])
        extends EUM with MR[Story, CreateStory, EUE]

case class UpdateStory(
        credentials: EUCC,
        storyId: String,
        title: String,
        imageId: Option[String] = None,
        community: String,
        productInfo: Option[String]) extends EUM with EUI with SI

case class UpdateStoryResponse(message: UpdateStory, value: Either[EUE, Story])
        extends EUM with MR[Story, UpdateStory, EUE]


case class CreateChapter(
        credentials: EUCC,
        storyId: String,
        title: String,
        text: String,
        imageIds: List[String],
        publish: Option[Boolean] = None) extends EUM with EUI with SI

case class CreateChapterResponse(message: CreateChapter, value: Either[EUE, ChapterInfo])
        extends EUM with MR[ChapterInfo, CreateChapter, EUE]

case class UpdateCommunity(
        credentials: EUCC,
        storyId: String,
        communityId: String) extends EUM with EUI with SI

case class UpdateCommunityResponse(message: UpdateCommunity, value: Either[EUE, Story])
        extends EUM with MR[Story, UpdateCommunity, EUE]

case class UpdateChapter(
        credentials: EUCC,
        storyId: String,
        chapterId: String,
        title: String,
        text: String,
        imageIds: List[String] = List.empty[String],
        publish: Option[Boolean] = None) extends EUM with EUI with SI

case class UpdateChapterResponse(message: UpdateChapter, value: Either[EUE, ChapterInfo])
        extends EUM with MR[ChapterInfo, UpdateChapter, EUE]


case class CreateComment(
        credentials: EUCC,
        storyOwnerId: String,
        storyId: String,
        chapterId: String,
        text: String,
        parentCommentId: Option[String]) extends EUM with EUI

case class CreateCommentResponse(message: CreateComment, value: Either[EUE, Comment])
        extends EUM with MR[Comment, CreateComment, EUE]


private[echoeduser] case class NewComment(
        credentials: EUCC,
        byEchoedUser: EchoedUser,
        storyId: String,
        chapterId: String,
        text: String,
        parentCommentId: Option[String]) extends EUM with EUI with SI

private[echoeduser] case class NewCommentResponse(message: NewComment, value: Either[EUE, Comment])
        extends EUM with MR[Comment, NewComment, EUE]


case class ModerateStory(
        credentials: EUCC,
        storyId: String,
        moderatedBy: EchoedClientCredentials,
        moderated: Boolean = true) extends EUM with EUI with SI
case class ModerateStoryResponse(message: ModerateStory, value: Either[EUE, ModerationDescription])
        extends EUM with MR[ModerationDescription, ModerateStory, EUE]


case class RequestImageUpload(credentials: EUCC, storyId: String, callback: String) extends EUM with EUI with SI
case class RequestImageUploadResponse(
        message: RequestImageUpload,
        value: Either[EUE, Map[String, String]]) extends EUM with MR[Map[String, String], RequestImageUpload, EUE]


case class StoryViewed(credentials: EUCC, storyId: String) extends EUM with EUI with SI

case class EchoTo(
        credentials: EUCC,
        echoId: String,
        facebookMessage: Option[String] = None,
        echoToFacebook: Boolean = false,
        twitterMessage: Option[String] = None,
        echoToTwitter: Boolean = false) extends EUM with EUI
case class EchoToResponse(message: EchoTo, value: Either[EUE, EchoFull]) extends EUM with MR[EchoFull, EchoTo, EUE]

private[echoeduser] case class EchoToFacebook(echo:Echo, echoMessage: Option[String]) extends EUM
private[echoeduser] case class EchoToFacebookResponse(message: EchoToFacebook, value: Either[EUE, FacebookPost])
    extends EUM with MR[FacebookPost, EchoToFacebook, EUE]

private[echoeduser] case class EchoToTwitter(echo:Echo, echoMessage: Option[String], hashTag: Option[String]) extends EUM
private[echoeduser] case class EchoToTwitterResponse(message:EchoToTwitter,  value: Either[EUE, TwitterStatus])
    extends EUM with MR[TwitterStatus, EchoToTwitter,  EUE]

case class PublishFacebookAction(credentials: EUCC, action: String,  obj: String,  objUrl: String) extends EUM with EUI
case class PublishFacebookActionResponse(message: PublishFacebookAction, value: Either[EUE, Boolean])
    extends EUM with MR[Boolean, PublishFacebookAction, EUE]

private[echoeduser] case class GetFriendExhibit(echoedFriendUserId: String, page: Int) extends EUM
private[echoeduser] case class GetFriendExhibitResponse(message: GetFriendExhibit,  value: Either[EUE, FriendCloset])
    extends EUM with MR[FriendCloset, GetFriendExhibit, EUE]

case class LoginWithPassword(credentials: EUCC) extends EUM with EUI
case class LoginWithPasswordResponse(message: LoginWithPassword, value: Either[EUE, EUCC])
    extends EUM with MR[EUCC, LoginWithPassword, EUE]

case class VerifyEmail(credentials: EUCC, code: String) extends EUM with EUI
case class VerifyEmailResponse(message: VerifyEmail, value: Either[EUE, EUCC])
    extends EUM with MR[EUCC, VerifyEmail, EUE]

case class RegisterLogin(
        name: String,
        email: String,
        screenName: String,
        password: String,
        credentials: Option[EUCC] = None) extends EUM
case class RegisterLoginResponse(message: RegisterLogin, value: Either[EUE, EUCC])
    extends EUM with MR[EUCC, RegisterLogin, EUE]

case class ResetLogin(credentials: EUCC) extends EUM with EUI
case class ResetLoginResponse(message: ResetLogin, value: Either[EUE, Boolean])
    extends EUM with MR[Boolean, ResetLogin, EUE]

case class ResetPassword(credentials: EUCC, code: String, password: String) extends EUM with EUI
case class ResetPasswordResponse(message: ResetPassword, value: Either[EUE, EUCC])
    extends EUM with MR[EUCC, ResetPassword,  EUE]

case class GetEchoedUser(credentials: EUCC) extends EUM with EUI
case class GetEchoedUserResponse(message: GetEchoedUser, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, GetEchoedUser,  EUE]

case class UpdateEchoedUserEmail(credentials: EUCC, email: String) extends EUM with EUI
case class UpdateEchoedUserEmailResponse(message: UpdateEchoedUserEmail, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, UpdateEchoedUserEmail, EUE]

case class GetExhibit(credentials: EUCC, page: Int, appType: Option[String] = None) extends EUM with EUI
case class GetExhibitResponse(message: GetExhibit, value: Either[EUE, ClosetPersonal])
    extends EUM with MR[ClosetPersonal, GetExhibit, EUE]

case class GetEcho(credentials: EUCC, echoId: String) extends EUM with EUI
case class GetEchoResponse(message: GetEcho, value: Either[EUE, (Echo, EchoedUser, Partner)])
        extends EUM with MR[(Echo, EchoedUser, Partner), GetEcho, EUE]

case class GetFeed(credentials: EUCC, page: Int) extends EUM with EUI
case class GetFeedResponse(message: GetFeed, value: Either[EUE, Feed])
    extends EUM with MR[Feed, GetFeed, EUE]

case class GetUserFeed(credentials: EUCC, page: Int) extends EUM with EUI
case class GetUserFeedResponse(message: GetUserFeed, value: Either[EUE, StoryFeed])
    extends EUM with MR[StoryFeed, GetUserFeed, EUE]

case class GetEchoedFriends(credentials: EUCC) extends EUM with EUI
case class GetEchoedFriendsResponse(message: GetEchoedFriends, value: Either[EUE, FriendFeed])
    extends EUM with MR[FriendFeed, GetEchoedFriends, EUE]

case class Logout(credentials: EUCC) extends EUM with EUI
case class LogoutResponse(message: Logout, value: Either[EUE, Boolean])
    extends EUM with MR[Boolean, Logout, EUE]

private[echoeduser] case class RegisterEchoedUserService(echoedUser: EchoedUser) extends EUM


private[echoeduser] case class ProcessImage(either: Either[Image, String]) extends EUM
private[echoeduser] case class ProcessImageResponse(
        message: ProcessImage,
        value: Either[EUE, Image]) extends EUM with MR[Image, ProcessImage, EUE]

case class AlreadyRegistered(email: String, m: String = "Echoed user already registered") extends EUE(m)


case class InvalidCredentials(m: String = "Invalid email or password") extends EUE(m)

case class EchoNotFound(id: String, m: String = "Echo not found %s") extends EUE(m format id)

case class InvalidRegistration(_errors: Errors, m: String = "Error creating account") extends EUE(m, errors = Some(_errors))
