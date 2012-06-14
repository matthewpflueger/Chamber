package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.domain._
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class EchoedUserServiceActorClient(echoedUserServiceActor: ActorRef) extends EchoedUserService with ActorClient {

    private implicit val timeout = Timeout(20 seconds)

    def getEchoedUser() =
            (echoedUserServiceActor ? GetEchoedUser()).mapTo[GetEchoedUserResponse]

    def getProfile() =
            (echoedUserServiceActor ? GetProfile()).mapTo[GetProfileResponse]

    def updateEchoedUserEmail(email: String) =
            (echoedUserServiceActor ? UpdateEchoedUserEmail(email)).mapTo[UpdateEchoedUserEmailResponse]

    def updateEchoedUser(echoedUser: EchoedUser) =
            (echoedUserServiceActor ? UpdateEchoedUser(echoedUser)).mapTo[UpdateEchoedUserResponse]

    def assignFacebookService(facebookService:FacebookService) =
            (echoedUserServiceActor ? (AssignFacebookService(facebookService))).mapTo[AssignFacebookServiceResponse]

    def assignTwitterService(twitterService:TwitterService) =
            (echoedUserServiceActor ? (AssignTwitterService(twitterService))).mapTo[AssignTwitterServiceResponse]

    def echoTo(echoTo: EchoTo) =
        (echoedUserServiceActor ? echoTo).mapTo[EchoToResponse]

    def publishFacebookAction(action: String, obj: String, objUrl: String) =
        (echoedUserServiceActor ? PublishFacebookAction(action, obj, objUrl)).mapTo[PublishFacebookActionResponse]

    def getCloset = (echoedUserServiceActor ? GetExhibit(0)).mapTo[GetExhibitResponse]

    def getCloset(page: Int) = (echoedUserServiceActor ? GetExhibit(page)).mapTo[GetExhibitResponse]

    def getFeed = (echoedUserServiceActor ? GetFeed(0)).mapTo[GetFeedResponse]
    
    def getFeed(page: Int) = (echoedUserServiceActor ? GetFeed(page)).mapTo[GetFeedResponse]

    def getPartnerFeed(partnerId: String, page: Int) = (echoedUserServiceActor ? GetPartnerFeed(partnerId, page)).mapTo[GetPartnerFeedResponse]

    def getPublicFeed = (echoedUserServiceActor ? GetPublicFeed(0)).mapTo[GetPublicFeedResponse]
    
    def getPublicFeed(page: Int) = (echoedUserServiceActor ? GetPublicFeed(page)).mapTo[GetPublicFeedResponse]

    def getFriendCloset(echoedFriendId: String) =
            (echoedUserServiceActor ? GetFriendExhibit(echoedFriendId, 0)).mapTo[GetFriendExhibitResponse]
    
    def getFriendCloset(echoedFriendId: String, page: Int) = 
            (echoedUserServiceActor ? GetFriendExhibit(echoedFriendId, page)).mapTo[GetFriendExhibitResponse]

    def getFriends = (echoedUserServiceActor ? GetEchoedFriends()).mapTo[GetEchoedFriendsResponse]

    def actorRef = echoedUserServiceActor

    val id = echoedUserServiceActor.toString

    def logout(echoedUserId: String) = (echoedUserServiceActor ? Logout(echoedUserId)).mapTo[LogoutResponse]

    override def toString = id
}
