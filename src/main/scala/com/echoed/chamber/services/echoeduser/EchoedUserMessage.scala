package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.views.{Feed,Closet}
import com.echoed.chamber.domain.{EchoedUser,EchoedFriend,Echo,TwitterStatus,FacebookPost}
import com.echoed.chamber.services.facebook.{FacebookService}
import com.echoed.chamber.services.twitter.{TwitterService}


sealed trait EchoedUserMessage extends Message
sealed case class EchoedUserException(message: String = "", cause: Throwable = null) extends EchoedException(message,cause)

import com.echoed.chamber.services.echoeduser.{EchoedUserMessage => EUM}
import com.echoed.chamber.services.echoeduser.{EchoedUserException => EUE}

case class AssignFacebookService(facebookService: FacebookService) extends EUM
case class AssignFacebookServiceResponse(message: AssignFacebookService, value: Either[EchoedUserException, FacebookService])
    extends EUM with RM[FacebookService, AssignFacebookService, EUE]

case class AssignTwitterService(twitterService: TwitterService) extends EUM
case class AssignTwitterServiceResponse(message: AssignTwitterService, value: Either[EchoedUserException,TwitterService])
    extends EUM with RM[TwitterService,  AssignTwitterService, EUE]

case class EchoToFacebook(echo:Echo, echoMessage: String) extends EUM
case class EchoToFacebookResponse(message: EchoToFacebook, value: Either[EchoedUserException, FacebookPost])
    extends EUM with RM[FacebookPost, EchoToFacebook, EUE]

case class EchoToTwitter(echo:Echo, echoMessage: String) extends EUM
case class EchoToTwitterResponse(message:EchoToTwitter,  value: Either[EchoedUserException, TwitterStatus])
    extends EUM with RM[TwitterStatus, EchoToTwitter,  EUE]

case class GetFriendExhibit(echoedFriendUserId: String) extends EUM
case class GetFriendExhibitResponse(message: GetFriendExhibit,  value: Either[EchoedUserException, Closet])
    extends EUM with RM[Closet, GetFriendExhibit, EUE]

case class GetEchoedUser() extends EUM
case class GetEchoedUserResponse(message: GetEchoedUser, value: Either[EchoedUserException, EchoedUser])
    extends EUM with RM[EchoedUser, GetEchoedUser,  EUE]

case class GetExhibit() extends EUM
case class GetExhibitResponse(message: GetExhibit, value: Either[EchoedUserException, Closet])
    extends EUM with RM[Closet, GetExhibit, EUE]

case class GetFeed() extends EUM
case class GetFeedResponse(message: GetFeed, value: Either[EchoedUserException, Feed])
    extends EUM with RM[Feed,GetFeed,EUE]

case class GetEchoedFriends() extends EUM
case class GetEchoedFriendsResponse(message: GetEchoedFriends, value: Either[EchoedUserException, List[EchoedFriend]])
    extends EUM with RM[List[EchoedFriend],GetEchoedFriends,EUE]

case class LocateWithId(echoedUserId: String) extends EUM
case class LocateWithIdResponse(message: LocateWithId, value: Either[EchoedUserException, EchoedUserService])
    extends EUM with RM[EchoedUserService,LocateWithId,EUE]

case class LocateWithFacebookService(facebookService: FacebookService) extends EUM
case class LocateWithFacebookServiceResponse(
    message: LocateWithFacebookService,
    value: Either[EchoedUserException, EchoedUserService])
    extends EUM with RM[EchoedUserService,LocateWithFacebookService,EUE]

case class LocateWithTwitterService(twitterService: TwitterService) extends EUM
case class LocateWithTwitterServiceResponse(message: LocateWithTwitterService,value: Either[EchoedUserException,EchoedUserService])
    extends EUM with RM[EchoedUserService, LocateWithTwitterService, EUE]

case class CreateEchoedUserServiceWithId(echoedUserId: String) extends EUM
case class CreateEchoedUserServiceWithIdResponse(message: CreateEchoedUserServiceWithId, value: Either[EchoedUserException,EchoedUserService]) 
    extends EUM with RM[EchoedUserService, CreateEchoedUserServiceWithId,EUE]

case class CreateEchoedUserServiceWithFacebookService(facebookService: FacebookService) extends EUM
case class CreateEchoedUserServiceWithFacebookServiceResponse(message: CreateEchoedUserServiceWithFacebookService, value: Either[EchoedUserException,EchoedUserService]) 
    extends EUM with RM[EchoedUserService, CreateEchoedUserServiceWithFacebookService,EUE]

case class CreateEchoedUserServiceWithTwitterService(twitterService: TwitterService) extends EUM
case class CreateEchoedUserServiceWithTwitterServiceResponse(message: CreateEchoedUserServiceWithTwitterService, value: Either[EchoedUserException, EchoedUserService])
    extends EUM with RM[EchoedUserService, CreateEchoedUserServiceWithTwitterService, EUE]