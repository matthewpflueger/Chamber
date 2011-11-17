package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import akka.dispatch.Future
import com.echoed.chamber.domain.{EchoedUser, EchoPossibility,TwitterFollower,TwitterStatus,TwitterUser}
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.domain.{FacebookPost, Echo, EchoedUser}
import akka.util.Duration
import org.slf4j.{LoggerFactory, Logger}



class EchoedUserServiceActorClient(echoedUserServiceActor: ActorRef) extends EchoedUserService {

    private final val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceActorClient])

    def getEchoedUser() =
            (echoedUserServiceActor ? "echoedUser").mapTo[EchoedUser]

    def assignFacebookService(facebookService:FacebookService) =
            (echoedUserServiceActor ? ("assignFacebookService",facebookService)).mapTo[FacebookService]

    def assignTwitterService(twitterService:TwitterService) =
            (echoedUserServiceActor ? ("assignTwitterService", twitterService)).mapTo[TwitterService]

    def updateTwitterStatus(status:String) =
            (echoedUserServiceActor ? ("updateTwitterStatus", status)).mapTo[TwitterStatus]

    def getTwitterFollowers() =
            (echoedUserServiceActor ? ("getTwitterFollowers")).mapTo[Array[TwitterFollower]]

    def echoToFacebook(echo: Echo, message: String) =
            (echoedUserServiceActor ? ("echoToFacebook", echo, message)).mapTo[FacebookPost]


}
