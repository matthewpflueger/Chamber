package com.echoed.chamber.services.facebook

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.views.FacebookPostData
import com.echoed.chamber.domain._


sealed trait FacebookMessage extends Message

sealed case class FacebookException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)


import com.echoed.chamber.services.facebook.{FacebookMessage => FM}
import com.echoed.chamber.services.facebook.{FacebookException => FE}


case class AssignEchoedUser(echoedUser: EchoedUser) extends FM
case class AssignEchoedUserResponse(message: AssignEchoedUser, value: Either[FE, FacebookUser])
        extends FM
        with RM[FacebookUser, AssignEchoedUser, FE]

case class UpdateAccessToken(accessToken: String) extends FM
case class UpdateAccessTokenResponse(message: UpdateAccessToken, value: Either[FE, FacebookUser])
        extends FM
        with RM[FacebookUser, UpdateAccessToken, FE]

case class EchoToFacebook(echo: Echo, message: String) extends FM
case class EchoToFacebookResponse(message: EchoToFacebook, value: Either[FE, FacebookPost])
        extends FM
        with RM[FacebookPost, EchoToFacebook, FE]

case class Post(accessToken: String, facebookId: String, facebookPost: FacebookPost) extends FM
case class PostResponse(message: Post, value: Either[FE, FacebookPost])
        extends FM
        with RM[FacebookPost, Post, FE]

case class GetPostData(facebookPostData: FacebookPostData) extends FM
case class GetPostDataResponse(message: GetPostData, value: Either[FE, FacebookPostData])
        extends FM
        with RM[FacebookPostData, GetPostData, FE]

case class GetPostDataFalse(m: String = "Facebook returned false for post", facebookPost: FacebookPost) extends FE(m)

case class GetFriends(accessToken: String, facebookId: String, facebookUserId: String) extends FM
case class GetFriendsResponse(message: GetFriends, value: Either[FE, List[FacebookFriend]])
        extends FM
        with RM[List[FacebookFriend], GetFriends, FE]

case class FetchMe(accessToken: String) extends FM
case class FetchMeResponse(message: FetchMe, value: Either[FE, FacebookUser])
        extends FM
        with RM[FacebookUser, FetchMe, FE]

case class GetMe(code: String, queryString: String) extends FM
case class GetMeResponse(message: GetMe, value: Either[FE, FacebookUser])
        extends FM
        with RM[FacebookUser, GetMe, FE]

case class GetFacebookUser() extends FM
case class GetFacebookUserResponse(message: GetFacebookUser, value: Either[FE, FacebookUser])
        extends FM
        with RM[FacebookUser, GetFacebookUser, FE]

case class Logout(facebookUserId: String) extends FM
case class LogoutResponse(message: Logout, value: Either[FE, Boolean])
    extends FM with RM[Boolean, Logout, FE]

case class LocateByCode(code: String, queryString: String) extends FM
case class LocateByCodeResponse(message: LocateByCode, value: Either[FE, FacebookService])
    extends FM with RM[FacebookService, LocateByCode, FE]

case class LocateById(facebookUserId: String) extends FM
case class LocateByIdResponse(message: LocateById, value: Either[FE, FacebookService])
    extends FM with RM[FacebookService, LocateById, FE]

case class LocateByFacebookId(facebookId: String, accessToken: String) extends FM
case class LocateByFacebookIdResponse(message: LocateByFacebookId, value: Either[FE, FacebookService])
    extends FM with RM[FacebookService, LocateByFacebookId, FE]

case class CreateFromCode(code: String, queryString: String) extends FM
case class CreateFromCodeResponse(message: CreateFromCode, value: Either[FE, FacebookService])
    extends FM with RM[FacebookService, CreateFromCode, FE]

case class CreateFromId(facebookUserId: String) extends FM
case class CreateFromIdResponse(message: CreateFromId, value: Either[FE, FacebookService])
    extends FM with RM[FacebookService, CreateFromId, FE]

case class CreateFromFacebookId(facebookId: String, accessToken: String) extends FM
case class CreateFromFacebookIdResponse(message: CreateFromFacebookId, value: Either[FE, FacebookService])
    extends FM with RM[FacebookService, CreateFromFacebookId, FE]

case class FacebookUserNotFound(id: String, m: String = "Facebook user not found") extends FE(m)

