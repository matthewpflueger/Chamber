package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.{MessageResponse => MR, Correlated, EchoedClientCredentials, EchoedException, Message}
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.partner.{Partner, PartnerSettings}
import com.echoed.chamber.domain.views._
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.{FacebookAccessToken, FacebookCode}


sealed trait EchoedUserMessage extends Message
sealed case class EchoedUserException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

case class EchoedUserClientCredentials(
        id: String,
        name: Option[String] = None,
        email: Option[String] = None,
        screenName: Option[String] = None,
        facebookId: Option[String] = None,
        twitterId: Option[String] = None) extends EchoedClientCredentials {

    val echoedUserId = id
}


trait EchoedUserIdentifiable {
    this: EchoedUserMessage =>
    def credentials: EchoedUserClientCredentials
    val echoedUserId = credentials.echoedUserId
}


import com.echoed.chamber.services.echoeduser.{EchoedUserMessage => EUM}
import com.echoed.chamber.services.echoeduser.{EchoedUserException => EUE}
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials => EUCC}
import com.echoed.chamber.services.echoeduser.{EchoedUserIdentifiable => EUI}


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
        override val correlationSender: Option[ActorRef]) extends EUM with Correlated


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
        override val correlationSender: Option[ActorRef]) extends EUM with Correlated

case class LoginWithTwitter(oAuthToken: String, oAuthVerifier: String) extends EUM
case class LoginWithTwitterResponse(message: LoginWithTwitter, value: Either[EUE, EchoedUser])
        extends EUM with MR[EchoedUser, LoginWithTwitter, EUE]

case class GetTwitterAuthenticationUrl(callbackUrl: String) extends EUM
case class GetTwitterAuthenticationUrlResponse(message: GetTwitterAuthenticationUrl, value: Either[EUE, String])
        extends EUM with MR[String, GetTwitterAuthenticationUrl, EUE]


private[echoeduser] case class LoginWithCredentials(credentials: EUCC) extends EUM with EUI




case class InitStory(
        credentials: EUCC,
        storyId: Option[String] = None,
        echoId: Option[String] = None,
        partnerId: Option[String] = None) extends EUM with EUI

case class InitStoryResponse(message: InitStory, value: Either[EUE, StoryInfo])
        extends EUM with MR[StoryInfo, InitStory, EUE]


case class CreateStory(
        credentials: EUCC,
        title: String,
        imageId: String,
        partnerId: Option[String] = None,
        echoId: Option[String] = None,
        productInfo: Option[String] = None) extends EUM with EUI

case class CreateStoryResponse(message: CreateStory, value: Either[EUE, Story])
        extends EUM with MR[Story, CreateStory, EUE]

case class TagStory(
        credentials: EUCC,
        storyId: String,
        tagId: String) extends EUM with EUI
case class TagStoryResponse(message: TagStory, value: Either[EUE, Story])
        extends EUM with MR[Story, TagStory, EUE]


case class UpdateStory(
        credentials: EUCC,
        storyId: String,
        title: String,
        imageId: String) extends EUM with EUI

case class UpdateStoryResponse(message: UpdateStory, value: Either[EUE, Story])
        extends EUM with MR[Story, UpdateStory, EUE]


case class CreateChapter(
        credentials: EUCC,
        storyId: String,
        title: String,
        text: String,
        imageIds: Option[Array[String]]) extends EUM with EUI

case class CreateChapterResponse(message: CreateChapter, value: Either[EUE, ChapterInfo])
        extends EUM with MR[ChapterInfo, CreateChapter, EUE]


case class UpdateChapter(
        credentials: EUCC,
        chapterId: String,
        title: String,
        text: String,
        imageIds: Option[Array[String]] = None) extends EUM with EUI

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
        parentCommentId: Option[String]) extends EUM with EUI

private[echoeduser] case class NewCommentResponse(message: NewComment, value: Either[EUE, Comment])
        extends EUM with MR[Comment, NewComment, EUE]


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

case class PublishFacebookAction(credentials: EUCC, action: String,  obj: String,  objUrl: String, origin: String) extends EUM with EUI
case class PublishFacebookActionResponse(message: PublishFacebookAction, value: Either[EUE, Boolean])
    extends EUM with MR[Boolean, PublishFacebookAction, EUE]

case class GetFriendExhibit(echoedFriendUserId: String, page: Int) extends EUM
case class GetFriendExhibitResponse(message: GetFriendExhibit,  value: Either[EUE, FriendCloset])
    extends EUM with MR[FriendCloset, GetFriendExhibit, EUE]

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

case class EchoedUserNotFound(id: String, m: String = "Echoed user not found") extends EUE(m)


case class EchoNotFound(id: String, m: String = "Echo not found %s") extends EUE(m format id)