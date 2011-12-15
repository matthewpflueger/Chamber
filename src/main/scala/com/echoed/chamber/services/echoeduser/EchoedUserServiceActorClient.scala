package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.Closet


class EchoedUserServiceActorClient(echoedUserServiceActor: ActorRef) extends EchoedUserService {

    private final val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceActorClient])

    def getEchoedUser() =
            (echoedUserServiceActor ? "echoedUser").mapTo[EchoedUser]

    def assignFacebookService(facebookService:FacebookService) =
            (echoedUserServiceActor ? ("assignFacebookService",facebookService)).mapTo[FacebookService]

    def assignTwitterService(twitterService:TwitterService) =
            (echoedUserServiceActor ? ("assignTwitterService", twitterService)).mapTo[TwitterService]

//    def updateTwitterStatus(status:String) =
//            (echoedUserServiceActor ? ("updateTwitterStatus", status)).mapTo[TwitterStatus]

    def getTwitterFollowers() =
            (echoedUserServiceActor ? ("getTwitterFollowers")).mapTo[Array[TwitterFollower]]

    def echoToFacebook(echo: Echo, message: String) =
            (echoedUserServiceActor ? ("echoToFacebook", echo, message)).mapTo[FacebookPost]

    def echoToTwitter(echo:Echo, message:String) =
        (echoedUserServiceActor ? ("echoToTwitter",echo,message)).mapTo[TwitterStatus]

    def getCloset = (echoedUserServiceActor ? "closet").mapTo[Closet]
    
    def getFriendCloset(echoedFriendId: String) = (echoedUserServiceActor ? ("getFriendCloset",echoedFriendId)).mapTo[Closet]

    def friends = (echoedUserServiceActor ? 'friends).mapTo[List[EchoedFriend]]
}
