package com.echoed.chamber.services.facebook

import com.echoed.chamber.services.{EchoedException, MessageResponse => MR, Message}
import com.echoed.chamber.domain.views.FacebookPostData
import com.echoed.chamber.domain._
import akka.actor.ActorRef


sealed trait FacebookMessage extends Message

sealed class FacebookException(
        val message: String = "",
        val cause: Throwable = null,
        val errorType: String = null,
        val code: Int = 0,
        val subCode: Int = 0) extends EchoedException(message, cause) {
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


//case class EchoToFacebook(facebookUserId: String, echo: Echo, message: String) extends FM
//case class EchoToFacebookResponse(message: EchoToFacebook, value: Either[FE, FacebookPost])
//        extends FM
//        with MR[FacebookPost, EchoToFacebook, FE]

case class RetryEchoToFacebook(facebookPost: FacebookPost, retries: Int = 1) extends FM

case class PublishAction(accessToken: FacebookAccessToken, action: String, obj: String, objUrl: String) extends FM
case class PublishActionResponse(message: PublishAction, value: Either[FE , Boolean])
    extends FM
    with MR[Boolean, PublishAction, FE]

case class Post(accessToken: FacebookAccessToken, facebookPost: FacebookPost) extends FM
case class PostResponse(message: Post, value: Either[FE, FacebookPost])
        extends FM
        with MR[FacebookPost, Post, FE]

case class GetPostData(facebookPostData: FacebookPostData) extends FM
case class GetPostDataResponse(message: GetPostData, value: Either[FE, FacebookPostData])
        extends FM
        with MR[FacebookPostData, GetPostData, FE]

class GetPostDataError(
        val facebookPost: FacebookPost,
        val _errorType: String,
        val _cde: Int,
        val m: String) extends FE(m)

case class GetPostDataOAuthError(
       _facebookPost: FacebookPost,
       _type: String,
       _code: Int,
       _m: String) extends GetPostDataError(_facebookPost, _type, _code, _m)

case class GetPostDataFalse(m: String = "Facebook returned false for post", facebookPost: FacebookPost) extends FE(m)

case class FetchFriends(accessToken: FacebookAccessToken, facebookUserId: String) extends FM
case class FetchFriendsResponse(message: FetchFriends, value: Either[FE, List[FacebookFriend]])
        extends FM
        with MR[List[FacebookFriend], FetchFriends, FE]

case class FetchMe(either: Either[FacebookCode, FacebookAccessToken]) extends FM
case class FetchMeResponse(message: FetchMe, value: Either[FE, FacebookUser])
        extends FM
        with MR[FacebookUser, FetchMe, FE]

case class FacebookCode(code: String, queryString: String)
case class FacebookAccessToken(accessToken: String, facebookId: Option[String] = None)


sealed trait FacebookPostCrawlerMessage extends Message

case object CrawlNext extends FacebookPostCrawlerMessage