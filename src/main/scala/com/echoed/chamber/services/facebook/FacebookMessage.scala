package com.echoed.chamber.services.facebook

import com.echoed.chamber.services.{EchoedException, MessageResponse => MR, Message}
import com.echoed.chamber.domain.views.FacebookPostData
import com.echoed.chamber.domain._
import akka.actor.ActorRef


sealed trait FacebookMessage extends Message

sealed case class FacebookException(
        message: String = "",
        cause: Throwable = null,
        errorType: String = null,
        code: Int = 0,
        subCode: Int = 0) extends EchoedException(message, cause) {
    val oauthError = code == 190
    val unauthorized = code == 190 && subCode == 458
}

case class OAuthFacebookException(
        _message: String,
        _cause: Throwable = null,
        _errorType: String = null,
        _code: Int,
        _subCode: Int) extends FacebookException(_message, _cause, _errorType, _code, _subCode)


import com.echoed.chamber.services.facebook.{FacebookMessage => FM}
import com.echoed.chamber.services.facebook.{FacebookException => FE}


case class AssignEchoedUser(echoedUser: EchoedUser) extends FM
case class AssignEchoedUserResponse(message: AssignEchoedUser, value: Either[FE, FacebookUser])
        extends FM
        with MR[FacebookUser, AssignEchoedUser, FE]

case class UpdateAccessToken(accessToken: String) extends FM
case class UpdateAccessTokenResponse(message: UpdateAccessToken, value: Either[FE, FacebookUser])
        extends FM
        with MR[FacebookUser, UpdateAccessToken, FE]

case class EchoToFacebook(echo: Echo, message: String) extends FM
case class EchoToFacebookResponse(message: EchoToFacebook, value: Either[FE, FacebookPost])
        extends FM
        with MR[FacebookPost, EchoToFacebook, FE]

case class RetryEchoToFacebook(facebookPost: FacebookPost, retries: Int = 1) extends FM

case class PublishActionToFacebook(action: String, obj: String, objUrl: String) extends FM
case class PublishActionToFacebookResponse(message: PublishActionToFacebook, value: Either[FE, Boolean])
        extends FM
        with MR[Boolean, PublishActionToFacebook, FE]

case class PublishAction(accessToken: String, action: String, obj: String, objUrl: String) extends FM
case class PublishActionResponse(message: PublishAction, value: Either[FE , Boolean])
    extends FM
    with MR[Boolean, PublishAction, FE]

case class Post(accessToken: String, facebookId: String, facebookPost: FacebookPost) extends FM
case class PostResponse(message: Post, value: Either[FE, FacebookPost])
        extends FM
        with MR[FacebookPost, Post, FE]

case class GetPostData(facebookPostData: FacebookPostData) extends FM
case class GetPostDataResponse(message: GetPostData, value: Either[FE, FacebookPostData])
        extends FM
        with MR[FacebookPostData, GetPostData, FE]

case class GetPostDataError(
        facebookPost: FacebookPost,
        _errorType: String,
        _cde: Int,
        m: String) extends FE(m)

case class GetPostDataOAuthError(
       _facebookPost: FacebookPost,
       _type: String,
       _code: Int,
       _m: String) extends GetPostDataError(_facebookPost, _type, _code, _m)

case class GetPostDataFalse(m: String = "Facebook returned false for post", facebookPost: FacebookPost) extends FE(m)

case class GetFriends(accessToken: String, facebookId: String, facebookUserId: String, context: ActorRef) extends FM
case class GetFriendsResponse(message: GetFriends, value: Either[FE, List[FacebookFriend]])
        extends FM
        with MR[List[FacebookFriend], GetFriends, FE]

case class FetchMe(accessToken: String) extends FM
case class FetchMeResponse(message: FetchMe, value: Either[FE, FacebookUser])
        extends FM
        with MR[FacebookUser, FetchMe, FE]

case class GetMe(code: String, queryString: String) extends FM
case class GetMeResponse(message: GetMe, value: Either[FE, FacebookUser])
        extends FM
        with MR[FacebookUser, GetMe, FE]

case class GetFacebookUser() extends FM
case class GetFacebookUserResponse(message: GetFacebookUser, value: Either[FE, FacebookUser])
        extends FM
        with MR[FacebookUser, GetFacebookUser, FE]

case class Logout(facebookUserId: String) extends FM
case class LogoutResponse(message: Logout, value: Either[FE, Boolean])
    extends FM with MR[Boolean, Logout, FE]

case class LocateByCode(code: String, queryString: String) extends FM
case class LocateByCodeResponse(message: LocateByCode, value: Either[FE, FacebookService])
    extends FM with MR[FacebookService, LocateByCode, FE]

case class LocateById(facebookUserId: String) extends FM
case class LocateByIdResponse(message: LocateById, value: Either[FE, FacebookService])
    extends FM with MR[FacebookService, LocateById, FE]

case class LocateByFacebookId(facebookId: String, accessToken: String) extends FM
case class LocateByFacebookIdResponse(message: LocateByFacebookId, value: Either[FE, FacebookService])
    extends FM with MR[FacebookService, LocateByFacebookId, FE]

case class CreateFromCode(code: String, queryString: String) extends FM
case class CreateFromCodeResponse(message: CreateFromCode, value: Either[FE, FacebookService])
    extends FM with MR[FacebookService, CreateFromCode, FE]

case class CreateFromId(facebookUserId: String) extends FM
case class CreateFromIdResponse(message: CreateFromId, value: Either[FE, FacebookService])
    extends FM with MR[FacebookService, CreateFromId, FE]

case class CreateFromFacebookId(facebookId: String, accessToken: String) extends FM
case class CreateFromFacebookIdResponse(message: CreateFromFacebookId, value: Either[FE, FacebookService])
    extends FM with MR[FacebookService, CreateFromFacebookId, FE]

case class FacebookUserNotFound(id: String, m: String = "Facebook user not found") extends FE(m)

