package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future

import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.Closet
import com.echoed.chamber.domain.views.Feed


trait EchoedUserService {

    val id: String

    def getEchoedUser: Future[GetEchoedUserResponse]

    def getProfile: Future[GetProfileResponse]

    def updateEchoedUserEmail(email: String): Future[UpdateEchoedUserEmailResponse]

    def updateEchoedUser(echoedUser: EchoedUser): Future[UpdateEchoedUserResponse]

    def assignTwitterService(twitterService:TwitterService): Future[AssignTwitterServiceResponse]

    def assignFacebookService(facebookService:FacebookService): Future[AssignFacebookServiceResponse]

    //def getTwitterFollowers: Future[Array[TwitterFollower]]

    def echoTo(echoTo: EchoTo): Future[EchoToResponse]

    def publishFacebookAction(action: String, obj: String, objUrl: String): Future[PublishFacebookActionResponse]

    //def echoToFacebook(echo: Echo, message: Option[String]): Future[EchoToFacebookResponse]

    //def echoToTwitter(echo:Echo,  message: Option[String]): Future[EchoToTwitterResponse]

    def getCloset: Future[GetExhibitResponse]

    def getCloset(page: Int): Future[GetExhibitResponse]

    def getFeed: Future[GetFeedResponse]

    def getFeed(page: Int): Future[GetFeedResponse]

    def getFriendCloset(echoedFriendId: String): Future[GetFriendExhibitResponse]
    
    def getFriendCloset(echoedFriendId: String, page: Int): Future[GetFriendExhibitResponse]

    def getFriends: Future[GetEchoedFriendsResponse]
    
    def logout(echoedUserId: String): Future[LogoutResponse]
}

