package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future
import com.echoed.util.FutureHelper

import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.Closet
import com.echoed.chamber.domain.views.Feed


trait EchoedUserService {

    def echoedUserFun: () => Option[EchoedUser] = () => FutureHelper.get(getEchoedUser _)

    def echoedUser: Option[EchoedUser] = FutureHelper.get(getEchoedUser _)

    def getEchoedUser: Future[EchoedUser]


    def assignTwitterService(twitterService:TwitterService): Future[TwitterService]
    def assignFacebookService(facebookService:FacebookService): Future[FacebookService]

    // TWITTER RELATED FUNCTIONS
    //def updateTwitterStatus(status:String): Future[TwitterStatus]
    def getTwitterFollowers: Future[Array[TwitterFollower]]

    def echoToFacebook(echo: Echo, message: String): Future[FacebookPost]
    def echoToTwitter(echo:Echo,  message:String): Future[TwitterStatus]

    def getCloset: Future[GetExhibitResponse]

    def getFeed: Future[GetFeedResponse]
    
    def getFriendCloset(echoedFriendId: String): Future[GetFriendExhibitResponse]
    
    def getFriends: Future[GetEchoedFriendsResponse]

    def friends: Future[List[EchoedFriend]]
}

