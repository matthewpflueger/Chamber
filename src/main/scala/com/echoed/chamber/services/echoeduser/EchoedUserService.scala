package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.domain.EchoedUser
import akka.dispatch.Future
import org.slf4j.LoggerFactory
import com.echoed.util.FutureHelper
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.domain.{TwitterFollower,TwitterStatus, TwitterUser}

trait EchoedUserService {

    private final val logger = LoggerFactory.getLogger(classOf[EchoedUserService])

    def echoedUser: Option[EchoedUser] = FutureHelper.get(getEchoedUser _)
//    ){
//        try {
//            Option(getEchoedUser.get)
//        } catch {
//            case e =>
//                logger.error("Error retrieving EchoedUser {}", e)
//                None
//        }
//    }

    def getEchoedUser(): Future[EchoedUser]

    def assignTwitterService(twitterService:TwitterService): Future[TwitterService]
    def assignFacebookService(facebookService:FacebookService): Future[FacebookService]

    // TWITTER RELATED FUNCTIONS
    def updateTwitterStatus(status:String): Future[TwitterStatus]
    def getTwitterFollowers(): Future[Array[TwitterFollower]]


}