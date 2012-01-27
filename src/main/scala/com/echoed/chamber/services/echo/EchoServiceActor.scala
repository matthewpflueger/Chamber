package com.echoed.chamber.services.echo

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
import akka.actor.{Channel, Actor}


class EchoServiceActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoServiceActor])

    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var retailerViewDao: RetailerViewDao = _
    @BeanProperty var retailerDao: RetailerDao = _
    @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoMetricsDao: EchoMetricsDao = _
    @BeanProperty var echoClickDao: EchoClickDao = _


    def receive = {
        case msg @ RecordEchoPossibility(echoPossibility: EchoPossibility) =>
            import com.echoed.chamber.services.echo.{RecordEchoPossibilityResponse => REPR}

            val channel: Channel[REPR] = self.channel

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

                try {
                    //this checks to see if we have the minimum info for recording an echo possibility...
                    Option(echoPossibility.id).get
                    val epv = new EchoPossibilityView(echoPossibility, retailer.get, retailerSettings.get)

                    echo.cata(
                        ec => channel ! REPR(msg, Left(EchoExists(epv.copy(echo = ec), "Item already echoed"))),
                        {
                            channel ! REPR(msg, Right(epv))
                            echoPossibilityDao.insertOrUpdate(echoPossibility)
                            logger.debug("Recorded {}", echoPossibility)
                        })
                } catch {
                    case e: NoSuchElementException =>
                        channel ! REPR(msg, Left(EchoException("Invalid echo possibility", e)))
                        logger.debug("Invalid echo possibility: %s" format echoPossibility)
                }
            }).onException {
                case e =>
                    channel ! REPR(msg, Left(EchoException("Unexpected error", e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ GetEcho(echoPossibilityId) =>
            val channel: Channel[GetEchoResponse] = self.channel

            Future {
                Option(echoDao.findByEchoPossibilityId(echoPossibilityId)).cata(
                    echo => channel ! GetEchoResponse(msg, Right(echo)),
                    channel ! GetEchoResponse(msg, Left(EchoNotFound(echoPossibilityId)))
                )
            }.onException {
                case e =>
                    channel ! GetEchoResponse(msg, Left(EchoException("Could not get echo", e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ GetEchoPossibility(echoPossibilityId) =>
            val channel: Channel[GetEchoPossibilityResponse] = self.channel

            Future {
                Option(echoPossibilityDao.findById(echoPossibilityId)).cata(
                    ep => channel ! GetEchoPossibilityResponse(msg, Right(ep)),
                    channel ! GetEchoPossibilityResponse(msg, Left(EchoPossibilityNotFound(echoPossibilityId)))
                )
            }.onException {
                case e =>
                    channel ! GetEchoPossibilityResponse(msg, Left(EchoException("Could not get echo possibility", e)))
                    logger.error("Error processing %s" format msg, e)
            }



        case msg @ RecordEchoClick(echoClick, postId) =>
            val channel: Channel[RecordEchoClickResponse] = self.channel

            def error(e: Throwable) {
                channel ! RecordEchoClickResponse(msg, Left(EchoException("Could not record echo click", e)))
                logger.error("Error processing %s" format msg, e)
            }

            Future {
                Option(echoDao.findById(echoClick.echoId)).cata(
                    echo => {
                        channel.tryTell(RecordEchoClickResponse(msg, Right(echo)))

                        Future {
                            val ec = determinePostId(echo, echoClick, postId)
                            echoClickDao.insert(ec)
                            logger.debug("Successfully recorded click for Echo {}", echo.id)
                        }.onException {
                            case e => logger.error("Failed to save echo click %s for %s" format(echoClick, echo))
                        }

                        val emf = Future { Option(echoMetricsDao.findById(echo.echoMetricsId)).get }
                        val rsf = Future { Option(retailerSettingsDao.findById(echo.retailerSettingsId)).get }

                        (for {
                            echoMetrics <- emf
                            retailerSettings <- rsf
                        } yield {
                            val clickedEcho = echoMetrics.clicked(retailerSettings)
                            echoMetricsDao.updateForClick(clickedEcho)
                            logger.debug("Successfully updated click metrics for {}", clickedEcho.echoId)
                        }).onException {
                            case e => logger.error("Failed to save echo click metrics for %s" format echo, e)
                        }
                    },
                    {
                        channel ! RecordEchoClickResponse(msg, Left(EchoNotFound(echoClick.echoId)))
                        logger.error("Did not find echo to record click, postId {}, {}", postId, echoClick)
                    })
            }.onException { case e => error(e) }
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
