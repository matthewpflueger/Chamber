package com.echoed.chamber.services

import akka.actor.Actor
import reflect.BeanProperty
import akka.dispatch.Future
import com.echoed.chamber.domain._

import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}

import org.slf4j.LoggerFactory
import com.echoed.chamber.dao.{EchoClickDao, EchoDao, RetailerDao, EchoPossibilityDao}
import java.util.regex.Pattern
import java.net.URI


class EchoServiceActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoServiceActor])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var retailerDao: RetailerDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoClickDao: EchoClickDao = _


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

            val channel = self.channel
            futureEchoedUserService.map {
                echoedUserService =>
                    val futureFacebookPost = echoedUserService.echoToFacebook(echo, message).map[FacebookPost] { facebookPost =>
                        echo.facebookPostId = facebookPost.id
                        echoDao.updateFacebookPostId(echo)
                        logger.debug("Successfully echoed {} to Facebook {}", echo, facebookPost)
                        facebookPost
                    }
                    /*val futureTwitterStatus = echoedUserService.echoToTwitter(echo,message).map[TwitterStatus] { twitterStatus =>
                        echo.twitterStatusId = twitterStatus.id
                        echoDao.updateTwitterStatusId(echo)
                        logger.debug("Successfully echoed {} to Twitter {}", echo, twitterStatus)
                        //twitterStatus
                        null
                    } */
                    for {
                        facebookPost <- futureFacebookPost
                        //twitterStatus <- futureTwitterStatus
                    } yield channel ! (echo, facebookPost)//, twitterStatus)
            }

            Future {
                echoPossibility.echoId = echo.id
                echoPossibility.step = "echoed"
                echoPossibilityDao.insertOrUpdate(echoPossibility)
            }
        }

        case ("recordEchoClick", echoClick: EchoClick, postId: String) =>
            val channel = self.channel
            Future[Echo] {
                echoDao.findById(echoClick.echoId)
            }.map { Option(_) match {
                    case None => logger.error("Did not find echo to record click {}", echoClick)
                    case Some(echo) =>
                        logger.debug("Recording click {} for {}", echoClick, echo)
                        val ec = determinePostId(echo, echoClick, postId)
                        channel ! (ec, echo.landingPageUrl)
                        echoClickDao.insert(ec)
                        logger.debug("Successfully recorded click {}", echoClick)
                }
            }
    }

    def determinePostId(echo: Echo, echoClick: EchoClick, postId: String) =
        Option(postId) match {
            case Some(f) if echo.facebookPostId == f  => echoClick.copy(facebookPostId = postId)
            case Some(t) if echo.twitterStatusId == t => echoClick.copy(twitterStatusId = postId)
            case Some(_) =>
                logger.warn("Invalid post id {}", postId)
                echoClick
            case None =>
                logger.warn("Null post id")
                echoClick
        }

}
