package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import akka.dispatch.Future._
import akka.dispatch.Future
import com.echoed.chamber.domain.{EchoedUser, EchoPossibility,TwitterFollower,TwitterStatus,TwitterUser}
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService


class EchoedUserServiceActorClient(echoedUserServiceActor: ActorRef) extends EchoedUserService {

    def getEchoedUser() =
        Future[EchoedUser] {
            (echoedUserServiceActor ? "echoedUser").get.asInstanceOf[EchoedUser]
        }

    def assignFacebookService(facebookService:FacebookService) = {
        Future[FacebookService] {
            (echoedUserServiceActor ? ("assignFacebookService",facebookService)).get.asInstanceOf[FacebookService]
        }
    }

    def assignTwitterService(twitterService:TwitterService) = {
        Future[TwitterService] {
            (echoedUserServiceActor ? ("assignTwitterService", twitterService)).get.asInstanceOf[TwitterService]
        }
    }

    def updateTwitterStatus(status:String) = {
        Future[TwitterStatus] {
            (echoedUserServiceActor ? ("updateTwitterStatus", status)).get.asInstanceOf[TwitterStatus]
        }
    }

    def getTwitterFollowers() = {
        Future[Array[TwitterFollower]] {
            (echoedUserServiceActor ? ("getTwitterFollowers")).get.asInstanceOf[Array[TwitterFollower]]
        }
    }
}
