package com.echoed.chamber.services.facebook

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.{FacebookLike, FacebookComment, FacebookPost, RetailerUser}
import com.echoed.chamber.domain.views.FacebookPostData


sealed trait FacebookMessage extends Message

sealed case class FacebookException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)


import com.echoed.chamber.services.facebook.{FacebookMessage => FM}
import com.echoed.chamber.services.facebook.{FacebookException => FE}

case class GetPostData(facebookPostData: FacebookPostData) extends FM
case class GetPostDataResponse(message: GetPostData, value: Either[FE, FacebookPostData])
        extends FM
        with RM[FacebookPostData, GetPostData, FE]



