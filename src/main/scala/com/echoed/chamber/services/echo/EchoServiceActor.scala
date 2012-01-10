package com.echoed.chamber.services.echo

import akka.actor.Actor
import reflect.BeanProperty
import akka.dispatch.Future
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator

import org.slf4j.LoggerFactory
import scala.Option
import com.echoed.chamber.dao.views.RetailerViewDao
import com.echoed.chamber.dao._
import com.echoed.chamber.domain.views.EchoPossibilityView
import com.echoed.chamber.domain.{EchoPossibility, EchoClick, Echo}
import java.util.Date

import scalaz._
import Scalaz._


class EchoServiceActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoServiceActor])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var retailerViewDao: RetailerViewDao = _
    @BeanProperty var retailerDao: RetailerDao = _
    @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoClickDao: EchoClickDao = _


    def receive = {
        case msg @ RecordEchoPossibility(echoPossibility: EchoPossibility) =>
            import com.echoed.chamber.services.echo.{RecordEchoPossibilityResponse => REPR}

            val channel = self.channel

            logger.debug("Processing {}", msg)

            val retailerFuture = Future {
                Option(retailerDao.findById(echoPossibility.retailerId))
            }
            val retailerSettingsFuture = Future {
                Option(retailerSettingsDao.findByActiveOn(echoPossibility.retailerId, new Date))
            }
            val echoFuture = Future {
                Option(echoDao.findByEchoPossibilityId(echoPossibility.id))
            }

            (for {
                retailer <- retailerFuture
                retailerSettings <- retailerSettingsFuture
                echo <- echoFuture
            } yield {
                logger.debug("Recording {}", echoPossibility)

                //this checks to see if we have the minimum info for recording an echo possibility...
                Option(echoPossibility.id).get
                val epv = new EchoPossibilityView(echoPossibility, retailer.get, retailerSettings.get)

                echo.cata(
                    ec => channel ! REPR(msg, Left(EchoExistsException(epv.copy(echo = ec), "Item already echoed"))),
                    {
                        channel ! REPR(msg, Right(epv))
                        echoPossibilityDao.insertOrUpdate(echoPossibility)
                        logger.debug("Recorded {}", echoPossibility)
                    })
            }).onException {
                case e: NoSuchElementException =>
                    channel ! REPR(msg, Left(EchoException("Invalid echo possibility", e)))
                    logger.warn("Invalid echo possibility: %s" format echoPossibility)
                case e =>
                    channel ! REPR(msg, Left(EchoException("Unexpected error", e)))
                    logger.error("Unexpected error recording echo possibility %s" format echoPossibility, e)
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
