package com.echoed.chamber.services

import akka.actor.Actor
import reflect.BeanProperty
import akka.dispatch.Future
import com.echoed.chamber.domain._


import com.echoed.chamber.services.echoeduser.{EchoedUserServiceLocator,EchoedUserService}
import com.echoed.chamber.dao.{EchoDao, RetailerDao, EchoPossibilityDao}
import com.echoed.chamber.dao.{EchoDao, RetailerDao, EchoPossibilityDao}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}

import org.slf4j.LoggerFactory


class EchoServiceActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoServiceActor])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var retailerDao: RetailerDao = _
    @BeanProperty var echoDao: EchoDao = _

    def receive = {
        case ("recordEchoPossibility", echoPossibility: EchoPossibility) => {
            val retailer = Future[Option[Retailer]] { Option(retailerDao.findById(echoPossibility.retailerId)) }
            (Option(echoPossibility.id), retailer.get) match {
                case (_, None) => throw new RuntimeException("Invalid retailerId in EchoPossibility %s " format echoPossibility)
                case (None, _) => throw new RuntimeException("Not enough information to record EchoPossibility %s" format echoPossibility)
                case _ => {
                    echoPossibilityDao.insertOrUpdate(echoPossibility)
                    self.channel ! echoPossibility
                }
            }
        }
        case ("echoPossibility", echoPossibilityId: String) => {
            self.channel ! Option(echoPossibilityDao.findById(echoPossibilityId)).getOrElse(None)
        }
        case ("echo", echoedUserId: String, echoPossibilityId: String, message: String) => {
            logger.debug(
                "Received echo request for user {} and echo possibility {} with message {}",
                Array(echoedUserId, echoPossibilityId, message))

            val futureEchoedUserService = echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId)
            val echoPossibility = echoPossibilityDao.findById(echoPossibilityId)
            val echo = new Echo(echoPossibility)
            echoDao.insert(echo)


//            //ECHO THE PURCHASE ON TWITTER...
//            //TODO still need some checks on status length. 120 Characters + Link
//            logger.debug("Echoing purchase on Twitter")
//            if(futureEchoedUserService.get.echoedUser.get.twitterUserId != null){
//              val twitterStatusMessage = "Check out the " + echoPossibility.getProductId() +  " I bought @ " + echoPossibility.getRetailerId() + " " + echoPossibility.getImageUrl()
//              futureEchoedUserService.get.updateTwitterStatus(twitterStatusMessage)
//            }

            val twitterStatusMessage = "Check out the " + echoPossibility.getProductId() +  " I bought @ " + echoPossibility.getRetailerId() + " " + echoPossibility.getImageUrl()
            val channel = self.channel
            futureEchoedUserService.map { echoedUserService =>
                val futureFacebookPost = echoedUserService.echoToFacebook(echo, message).map[FacebookPost] { facebookPost =>
                    echo.facebookPostId = facebookPost.id
                    echoDao.updateFacebookPostId(echo)
                    logger.debug("Successfully echoed {} to Facebook {}", echo, facebookPost)
                    facebookPost
                    //channel ! (echo, facebookPost)
                }
                val futureTwitterStatus = echoedUserService.updateTwitterStatus(twitterStatusMessage).map[TwitterStatus] { twitterStatus =>
                    //echo.twitterStatusId = twitterStatus.id
                    //echoDao.updateTwitterStatusId(echo)
                    logger.debug("Successfully echoed {} to Twitter {}", echo, twitterStatus)
                    twitterStatus
                }

                for {
                    facebookPost <- futureFacebookPost
                    twitterStatus <- futureTwitterStatus
                } yield channel ! (echo, facebookPost) //, twitterStatus)
            }

            Future {
                echoPossibility.echoId = echo.id
                echoPossibility.step = "echoed"
                echoPossibilityDao.insertOrUpdate(echoPossibility)
            }
        }
    }
}
