package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.{MessageResponse => MR, Correlated, EchoedClientCredentials, EchoedException, Message}
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.views._
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.{FacebookAccessToken, FacebookCode}
import scala.collection.immutable.Stack
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials


sealed trait EchoedUserMessage extends Message
sealed case class EchoedUserException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

case class EchoedUserClientCredentials(
        id: String,
        name: Option[String] = None,
        email: Option[String] = None,
        screenName: Option[String] = None,
        facebookId: Option[String] = None,
        twitterId: Option[String] = None) extends EchoedClientCredentials {

    def echoedUserId = id

}


trait EchoedUserIdentifiable {
    this: EchoedUserMessage =>
    def credentials: EchoedUserClientCredentials
    def echoedUserId = credentials.echoedUserId
}

trait EmailIdentifiable {
    this: EchoedUserMessage =>
    def email: String
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
        followedByUsers: List[Follower])


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
case class LoginWithFacebookResponse(message: LoginWithFacebook, value: Either[EUE, EchoedUser])
        extends EUM with MR[EchoedUser, LoginWithFacebook, EUE]


case class AddTwitter(
        credentials: EUCC,
        oAuthToken: String,
        oAuthVerifier: String) extends EUM with EUI

private[echoeduser] case class LoginWithTwitterUser(
        twitterUser: TwitterUser,
        correlation: LoginWithTwitter,
        override val correlationSender: Option[ActorRef]) extends EUM with Correlated[LoginWithTwitter]

case class LoginWithTwitter(oAuthToken: String, oAuthVerifier: String) extends EUM
case class LoginWithTwitterResponse(message: LoginWithTwitter, value: Either[EUE, EchoedUser])
        extends EUM with MR[EchoedUser, LoginWithTwitter, EUE]

case class GetTwitterAuthenticationUrl(callbackUrl: String) extends EUM
case class GetTwitterAuthenticationUrlResponse(message: GetTwitterAuthenticationUrl, value: Either[EUE, String])
        extends EUM with MR[String, GetTwitterAuthenticationUrl, EUE]


private[echoeduser] case class LoginWithCredentials(credentials: EUCC) extends EUM with EUI


case class FollowUser(credentials: EUCC, userToFollowerId: String) extends EUM with EUI
case class FollowUserResponse(message: FollowUser, value: Either[EUE, Boolean])
        extends EUM with MR[Boolean, FollowUser, EUE]

private[echoeduser] case class AddFollower(
        credentials: EUCC,
        echoedUser: EchoedUser,
        correlation: FollowUser,
        override val correlationSender: Option[ActorRef]) extends EUM with EUI with Correlated[FollowUser]

private[echoeduser] case class AddFollowerResponse(message: AddFollower, value: Either[EUE, EchoedUser])
        extends EUM with MR[EchoedUser, AddFollower, EUE]

case class UnFollowUser(credentials: EUCC, followingUserId: String) extends EUM with EUI
case class UnFollowUserResponse(message: UnFollowUser, value: Either[EUE, Boolean])
        extends EUM with MR[Boolean, UnFollowUser, EUE]

private[echoeduser] case class RemoveFollower(credentials: EUCC, echoedUser: EchoedUser) extends EUM with EUI
private[echoeduser] case class RemoveFollowerResponse(message: RemoveFollower, value: Either[EUE, Boolean])
        extends EUM with MR[Boolean, RemoveFollower, EUE]

case class Follower(echoedUserId: String, name: String, facebookId: Option[String] = None, twitterId: Option[String] = None)

case class ListFollowingUsers(credentials: EUCC) extends EUM with EUI
case class ListFollowingUsersResponse(message: ListFollowingUsers, value: Either[EUE, List[Follower]])
        extends EUM with MR[List[Follower], ListFollowingUsers, EUE]

case class ListFollowedByUsers(credentials: EUCC) extends EUM with EUI
case class ListFollowedByUsersResponse(message: ListFollowedByUsers, value: Either[EUE, List[Follower]])
        extends EUM with MR[List[Follower], ListFollowedByUsers, EUE]


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

case class EmailNotifications(credentials: EUCC) extends EUM with EUI

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
        partnerId: Option[String] = None) extends EUM with EUI

case class InitStoryResponse(message: InitStory, value: Either[EUE, StoryInfo])
        extends EUM with MR[StoryInfo, InitStory, EUE]

case class VoteStory(
        credentials: EUCC,
        storyOwnerId: String,
        storyId: String,
        value: Int) extends EUM with EUI with SI

case class VoteStoryResponse(message: VoteStory, value: Either[EUE, Boolean])
        extends EUM with MR[Boolean,  VoteStory, EUE]

case class NewVote(
        credentials: EUCC,
        byEchoedUser: EchoedUser,
        storyId: String,
        value: Int) extends EUM with EUI with SI

case class NewVoteResponse(message: NewVote, value: Either[EUE, Story])
        extends EUM with MR[Story, NewVote, EUE]


case class CreateStory(
        credentials: EUCC,
        title: String,
        imageId: Option[String] = None,
        partnerId: Option[String] = None,
        productInfo: Option[String] = None,
        echoId: Option[String] = None) extends EUM with EUI

case class CreateStoryResponse(message: CreateStory, value: Either[EUE, Story])
        extends EUM with MR[Story, CreateStory, EUE]

case class TagStory(
        credentials: EUCC,
        storyId: String,
        tagId: String) extends EUM with EUI with SI
case class TagStoryResponse(message: TagStory, value: Either[EUE, Story])
        extends EUM with MR[Story, TagStory, EUE]


case class UpdateStory(
        credentials: EUCC,
        storyId: String,
        title: String,
        imageId: String,
        productInfo: Option[String]) extends EUM with EUI with SI

case class UpdateStoryResponse(message: UpdateStory, value: Either[EUE, Story])
        extends EUM with MR[Story, UpdateStory, EUE]


case class CreateChapter(
        credentials: EUCC,
        storyId: String,
        title: String,
        text: String,
        imageIds: List[String],
        publish: Option[Boolean]) extends EUM with EUI with SI

case class CreateChapterResponse(message: CreateChapter, value: Either[EUE, ChapterInfo])
        extends EUM with MR[ChapterInfo, CreateChapter, EUE]


case class UpdateChapter(
        credentials: EUCC,
        storyId: String,
        chapterId: String,
        title: String,
        text: String,
        imageIds: List[String] = List.empty[String],
        publish: Option[Boolean]) extends EUM with EUI with SI

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
        moderatedBy: Either[PartnerUserClientCredentials, AdminUserClientCredentials],
        moderated: Boolean = true) extends EUM with EUI with SI
case class ModerateStoryResponse(message: ModerateStory, value: Either[EUE, ModerationDescription])
        extends EUM with MR[ModerationDescription, ModerateStory, EUE]


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

case class GetFriendExhibit(echoedFriendUserId: String, page: Int) extends EUM
case class GetFriendExhibitResponse(message: GetFriendExhibit,  value: Either[EUE, FriendCloset])
    extends EUM with MR[FriendCloset, GetFriendExhibit, EUE]

case class LoginWithEmailPassword(email: String, password: String) extends EUM with EmailIdentifiable
case class LoginWithEmailPasswordResponse(message: LoginWithEmailPassword, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, LoginWithEmailPassword, EUE]


private[echoeduser] case class LoginWithEmail(
        email: String,
        correlation: EchoedUserMessage with EmailIdentifiable,
        override val correlationSender: Option[ActorRef]) extends EUM with Correlated[EchoedUserMessage with EmailIdentifiable]
private[echoeduser] case class LoginWithEmailResponse(message: LoginWithEmail, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, LoginWithEmail, EUE]

case class RegisterLogin(name: String, email: String, password: String) extends EUM with EmailIdentifiable
case class RegisterLoginResponse(message: RegisterLogin, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, RegisterLogin, EUE]

case class ResetLogin(email: String) extends EUM with EmailIdentifiable
case class ResetLoginResponse(message: ResetLogin, value: Either[EUE, String])
    extends EUM with MR[String, ResetLogin, EUE]

case class LoginWithCode(code: String) extends EUM
case class LoginWithCodeResponse(message: LoginWithCode, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, LoginWithCode, EUE]

case class ResetPassword(credentials: EUCC, password: String) extends EUM with EUI
case class ResetPasswordResponse(message: ResetPassword, value: Either[EUE, EchoedUser])
    extends EUM with MR[EchoedUser, ResetPassword,  EUE]

case class Login(credentials: EUCC) extends EUM with EUI
case class LoginResponse(message: Login, value: Either[EUE, EchoedUser]) extends EUM with MR[EchoedUser, Login,  EUE]

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

case class GetEchoedFriends(credentials: EUCC) extends EUM with EUI
case class GetEchoedFriendsResponse(message: GetEchoedFriends, value: Either[EUE, FriendFeed])
    extends EUM with MR[FriendFeed, GetEchoedFriends, EUE]

case class Logout(credentials: EUCC) extends EUM with EUI
case class LogoutResponse(message: Logout, value: Either[EUE, Boolean])
    extends EUM with MR[Boolean, Logout, EUE]

private[echoeduser] case class RegisterEchoedUserService(echoedUser: EchoedUser) extends EUM


case class AlreadyRegistered(email: String, m: String = "Echoed user already registered") extends EUE(m)

case class EchoedUserNotFound(id: String, m: String = "Echoed user not found") extends EUE(m)

case class InvalidCredentials(m: String = "Invalid email or password") extends EUE(m)

case class EchoNotFound(id: String, m: String = "Echo not found %s") extends EUE(m format id)