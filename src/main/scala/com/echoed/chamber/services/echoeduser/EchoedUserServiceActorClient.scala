package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.{Closet,Feed}
import com.echoed.chamber.services.ActorClient
import akka.util.Duration


class EchoedUserServiceActorClient(echoedUserServiceActor: ActorRef) extends EchoedUserService with ActorClient {

    def getEchoedUser() =
            (echoedUserServiceActor ? GetEchoedUser()).mapTo[GetEchoedUserResponse]

    def assignFacebookService(facebookService:FacebookService) =
            (echoedUserServiceActor ? (AssignFacebookService(facebookService))).mapTo[AssignFacebookServiceResponse]

    def assignTwitterService(twitterService:TwitterService) =
            (echoedUserServiceActor ? (AssignTwitterService(twitterService))).mapTo[AssignTwitterServiceResponse]

//    def getTwitterFollowers() =
//            (echoedUserServiceActor ? ("getTwitterFollowers")).mapTo[Array[TwitterFollower]]

    def echoTo(echoTo: EchoTo) =
        (echoedUserServiceActor ? echoTo).mapTo[EchoToResponse]
        //NOTE: echo'ing may take a while so a long timeout should be specified but as the default timeout is
        //set very high we will leave this unset for now...
//        (echoedUserServiceActor.?(echoTo)(timeout = Duration(30, "seconds"))).mapTo[EchoToResponse]

    def echoToFacebook(echo:Echo, message: Option[String]) =
        (echoedUserServiceActor ? (EchoToFacebook(echo, message))).mapTo[EchoToFacebookResponse]

    def echoToTwitter(echo:Echo,  message: Option[String]) =
        (echoedUserServiceActor ? (EchoToTwitter(echo,message))).mapTo[EchoToTwitterResponse]

    def getCloset = (echoedUserServiceActor ? GetExhibit()).mapTo[GetExhibitResponse]

    def getFeed = (echoedUserServiceActor ? GetFeed(0)).mapTo[GetFeedResponse]
    
    def getFeed(page: Int) = (echoedUserServiceActor ? GetFeed(page)).mapTo[GetFeedResponse]

    def getPublicFeed = (echoedUserServiceActor ? GetPublicFeed(0)).mapTo[GetPublicFeedResponse]
    
    def getPublicFeed(page: Int) = (echoedUserServiceActor ? GetPublicFeed(page)).mapTo[GetPublicFeedResponse]

    def getFriendCloset(echoedFriendId: String) =
            (echoedUserServiceActor ? GetFriendExhibit(echoedFriendId)).mapTo[GetFriendExhibitResponse]

    def getFriends = (echoedUserServiceActor ? GetEchoedFriends()).mapTo[GetEchoedFriendsResponse]

    def actorRef = echoedUserServiceActor

    val id = echoedUserServiceActor.id

    def logout(echoedUserId: String) = (echoedUserServiceActor ? Logout(echoedUserId)).mapTo[LogoutResponse]

    override def toString = id
}
