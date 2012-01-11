package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.{Closet,Feed}
import com.echoed.chamber.services.ActorClient


class EchoedUserServiceActorClient(echoedUserServiceActor: ActorRef) extends EchoedUserService with ActorClient {

    def getEchoedUser() =
            (echoedUserServiceActor ? GetEchoedUser()).mapTo[GetEchoedUserResponse]

    def assignFacebookService(facebookService:FacebookService) =
            (echoedUserServiceActor ? (AssignFacebookService(facebookService))).mapTo[AssignFacebookServiceResponse]

    def assignTwitterService(twitterService:TwitterService) =
            (echoedUserServiceActor ? (AssignTwitterService(twitterService))).mapTo[AssignTwitterServiceResponse]

    def getTwitterFollowers() =
            (echoedUserServiceActor ? ("getTwitterFollowers")).mapTo[Array[TwitterFollower]]

    def echoTo(echoTo: EchoTo) = (echoedUserServiceActor ? echoTo).mapTo[EchoToResponse]

    def echoToFacebook(echo:Echo, message: Option[String]) =
        (echoedUserServiceActor ? (EchoToFacebook(echo, message))).mapTo[EchoToFacebookResponse]

    def echoToTwitter(echo:Echo,  message: Option[String]) =
        (echoedUserServiceActor ? (EchoToTwitter(echo,message))).mapTo[EchoToTwitterResponse]

    def getCloset = (echoedUserServiceActor ? GetExhibit()).mapTo[GetExhibitResponse]

    def getFeed = (echoedUserServiceActor ? GetFeed()).mapTo[GetFeedResponse]
    
    def getPublicFeed = (echoedUserServiceActor ? GetPublicFeed()).mapTo[GetPublicFeedResponse]

    def getFriendCloset(echoedFriendId: String) = (echoedUserServiceActor ? GetFriendExhibit(echoedFriendId)).mapTo[GetFriendExhibitResponse]

    def getFriends = (echoedUserServiceActor ? GetEchoedFriends()).mapTo[GetEchoedFriendsResponse]

    def friends = (echoedUserServiceActor ? 'friends).mapTo[List[EchoedFriend]]

    def actorRef = echoedUserServiceActor
}
