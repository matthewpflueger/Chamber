package com.echoed.chamber.services.echo

import akka.actor.Actor
import reflect.BeanProperty
import akka.dispatch.Future
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.Echo
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.echoeduser.{LocateWithIdResponse,GetEchoedUserResponse,EchoToFacebookResponse,EchoToTwitterResponse}

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.views.EchoFull
import scala.Option
import com.echoed.chamber.services.EchoedException
import com.echoed.chamber.dao._
import java.util.{Calendar, Date}


class EchoServiceActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoServiceActor])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var retailerDao: RetailerDao = _
    @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoClickDao: EchoClickDao = _


    def receive = {
        case ("recordEchoPossibility", echoPossibility: EchoPossibility) => {
            val retailer = Future[Option[Retailer]] { Option(retailerDao.findById(echoPossibility.retailerId)) }
            logger.debug("Recording Echo Possibility {} ",echoPossibility.retailerId)
            (Option(echoPossibility.id), retailer.get) match {
                case (_, None) => throw new RuntimeException("Invalid retailerId in EchoPossibility %s " format echoPossibility)
                case (None, _) => throw new RuntimeException("Not enough information to record EchoPossibility %s" format echoPossibility)
                case _ => {
                    echoPossibilityDao.insertOrUpdate(echoPossibility)
                    self.channel ! echoPossibility
                }
            }
        }
        case ("getEcho", echoPossibilityId:String) =>{
            val echo = Option(echoDao.findByEchoPossibilityId(echoPossibilityId)).getOrElse(None)
            if (echo==None)
                self.channel ! (echo,"New")
            else
            self.channel ! (echo, "Exists")
        }

        case ("echoPossibility", echoPossibilityId: String) => {
            self.channel ! Option(echoPossibilityDao.findById(echoPossibilityId)).getOrElse(None)
        }

        case ("recordEchoClick", echoClick: EchoClick, postId: String) =>
            val channel = self.channel
            Future[Echo] {
                echoDao.findById(echoClick.echoId)
            }.map { Option(_) match {
                    case None => logger.error("Did not find echo to record click {}", echoClick)
                    case Some(echo) =>
                        logger.debug("Recording {} for {}", echoClick, echo)
                        val ec = determinePostId(echo, echoClick, postId)
                        echoClickDao.insert(ec)
                        channel ! (ec, echo.landingPageUrl)
                        logger.debug("Successfully recorded {}", echoClick)

                        val retailerSettings = Option(retailerSettingsDao.findById(echo.retailerSettingsId)).get
                        val clickedEcho = echo.clicked(retailerSettings)
                        echoDao.updateForClick(clickedEcho)
                        logger.debug("Successfully updated for click {}", clickedEcho)
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
