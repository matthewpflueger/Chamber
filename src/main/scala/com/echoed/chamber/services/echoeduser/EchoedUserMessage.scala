package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.views.{Feed,Closet}
import com.echoed.chamber.domain.EchoedUser

sealed trait EchoedUserMessage extends Message
sealed case class EchoedUserException(message: String, cause: Throwable = null) extends EchoedException(message,cause)

import com.echoed.chamber.services.echoeduser.{EchoedUserMessage => EUM}
import com.echoed.chamber.services.echoeduser.{EchoedUserException => EUE}

case class GetFriendExhibit(echoedFriendUserId: String) extends EUM
case class GetFriendExhibitResponse(message: GetFriendExhibit,  value: Either[EchoedUserException, Closet])
    extends EUM with RM[Closet, GetFriendExhibit, EUE]

case class GetExhibit() extends EUM
case class GetExhibitResponse(message: GetExhibit, value: Either[EchoedUserException, Closet])
    extends EUM with RM[Closet, GetExhibit, EUE]

case class GetFeed() extends EUM
case class GetFeedResponse(message: GetFeed, value: Either[EchoedUserException, Feed])
    extends EUM with RM[Feed,GetFeed,EUE]

