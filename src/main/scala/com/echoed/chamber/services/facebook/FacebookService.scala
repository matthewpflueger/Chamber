package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.{EchoedUser, FacebookUser}
import com.echoed.util.FutureHelper


trait FacebookService {



    private final val logger = LoggerFactory.getLogger(classOf[FacebookService])

    def facebookUser: Option[FacebookUser] = FutureHelper.get[FacebookUser](getFacebookUser _)
//    def facebookUser: Option[FacebookUser] = {
//        try {
//            Option(getFacebookUser.get)
//        } catch {
//            case e =>
//                logger.error("Error {}", e)
//                None
//        }
//    }

    def getFacebookUser(): Future[FacebookUser]

    def assignEchoedUser(echoedUser: EchoedUser): Future[FacebookUser]
}