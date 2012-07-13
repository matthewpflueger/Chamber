package com.echoed.chamber.services.echo

import akka.dispatch.Future

import scala.Option
import com.echoed.chamber.dao._
import com.echoed.chamber.domain.views.EchoPossibilityView

import partner.{PartnerSettingsDao, PartnerDao}
import scalaz._
import Scalaz._
import com.echoed.chamber.domain.{EchoMetrics, EchoClick, Echo}
import com.echoed.util.TransactionUtils._
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.image.{ProcessImageResponse, ImageService}
import java.util.{UUID, Date}
import java.util.{List => JList, Collections}
import scala.collection.JavaConversions._
import com.echoed.chamber.services.{EchoedActor, EchoedException}
import akka.actor._
import akka.event.LoggingReceive


class EchoServiceActor(
        partnerDao: PartnerDao,
        partnerSettingsDao: PartnerSettingsDao,
        echoDao: EchoDao,
        echoMetricsDao: EchoMetricsDao,
        echoClickDao: EchoClickDao,
        imageDao: ImageDao,
        imageService: ImageService,
        transactionTemplate: TransactionTemplate,
        filteredUserAgents: JList[String] = Collections.emptyList[String]()) extends EchoedActor {

    def handle = {
        //TODO RecordEchoPossibility is deprecated and will be deleted asap!
        case msg @ RecordEchoPossibility(echoPossibility: Echo) =>
            import com.echoed.chamber.services.echo.{RecordEchoPossibilityResponse => REPR}

            val channel = context.sender
            implicit val ec = context.dispatcher

            log.debug("Processing {}", msg)

            val partnerFuture = Future {
                Option(partnerDao.findById(echoPossibility.partnerId))
            }
            val partnerSettingsFuture = Future {
                Option(partnerSettingsDao.findByActiveOn(echoPossibility.partnerId, new Date))
            }
            val echoFuture = Future {
                Option(echoDao.findByEchoPossibilityId(echoPossibility.echoPossibilityId))
            }

            (for {
                partner <- partnerFuture
                partnerSettings <- partnerSettingsFuture
                echo <- echoFuture
            } yield {
                log.debug("Recording {}", echoPossibility)

                try {
                    //this checks to see if we have the minimum info for recording an echo possibility...
                    Option(echoPossibility.echoPossibilityId).get
                    val epv = new EchoPossibilityView(echoPossibility, partner.get, partnerSettings.get)

                    echo.cata(
                        ec => {
                            if (ec.isEchoed) {
                                channel ! REPR(msg, Left(EchoExists(epv.copy(echo = ec), "Item already echoed")))
                            } else {
                                val ec2 = ec.copy(step = ("%s,%s" format(ec.step, echoPossibility.step)).takeRight(254))
                                echoDao.updateForStep(ec2)
                                channel ! REPR(msg, Right(epv.copy(echo = ec2)))
                            }
                        },
                        {

                            val ec = transactionTemplate.execute({status: TransactionStatus =>
                                var ec = echoPossibility.copy(partnerSettingsId = partnerSettings.get.id)
                                val echoMetrics = new EchoMetrics(ec, partnerSettings.get)
                                ec = ec.copy(echoMetricsId = echoMetrics.id)

                                val img = Option(imageDao.findByUrl(ec.image.url)).getOrElse {
                                    imageDao.insert(ec.image)
                                    imageService.processImage(ec.image).onComplete(_.fold(
                                        e => log.error("Unexpected error processing image for echo %s" format ec.id, e),
                                        _ match {
                                            case ProcessImageResponse(_, Left(e)) => log.error("Error processing image for echo %s" format ec.id, e)
                                            case ProcessImageResponse(_, Right(image)) => log.debug("Successfully processed image for echo {}", ec.id)
                                        }
                                    ))
                                    ec.image
                                }
                                echoMetricsDao.insert(echoMetrics)
                                ec = ec.copy(echoMetricsId = echoMetrics.id, image = img)
                                echoDao.insert(ec)
                                ec
                            })

                            channel ! REPR(msg, Right(epv.copy(echo = ec)))
                            log.debug("Recorded {}", ec)
                        })
                } catch {
                    case e: NoSuchElementException =>
                        channel ! REPR(msg, Left(EchoException("Invalid echo possibility", e)))
                        log.debug("Invalid echo possibility: %s" format echoPossibility)
                }
            }).onFailure {
                case e =>
                    channel ! REPR(msg, Left(EchoException("Unexpected error", e)))
                    log.error("Error processing %s" format msg, e)
            }

        case msg @ GetEcho(echoPossibilityId) =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            Future {
                Option(echoDao.findByEchoPossibilityId(echoPossibilityId)).cata(
                    echo => channel ! GetEchoResponse(msg, Right(echo)),
                    channel ! GetEchoResponse(msg, Left(EchoNotFound(echoPossibilityId)))
                )
            }.onFailure {
                case e =>
                    channel ! GetEchoResponse(msg, Left(EchoException("Could not get echo", e)))
                    log.error("Error processing %s" format msg, e)
            }

        case msg @ GetEchoById(echoId) =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            Future {
                Option(echoDao.findById(echoId)).cata(
                    echo => channel ! GetEchoByIdResponse(msg, Right(echo)),
                    channel ! GetEchoByIdResponse(msg, Left(EchoNotFound(echoId)))
                )
            }.onFailure {
                case e =>
                    channel ! GetEchoByIdResponse(msg, Left(EchoException("Could not get echo", e)))
                    log.error("Error processing %s" format msg, e)
            }

        case msg @ GetEchoByIdAndEchoedUserId(echoId, echoedUserId) =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            Future {
                Option(echoDao.findByIdAndEchoedUserId(echoId, echoedUserId)).cata(
                    echo => channel ! GetEchoByIdAndEchoedUserIdResponse(msg, Right(echo)),
                    channel ! GetEchoByIdAndEchoedUserIdResponse(msg, Left(EchoNotFound(echoId)))
                )
            }.onFailure {
                case e =>
                    channel ! GetEchoByIdAndEchoedUserIdResponse(msg, Left(EchoException("Could not get echo", e)))
                    log.error("Error processing %s" format msg, e)
            }

        case msg @ GetEchoPossibility(echoPossibilityId) =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            Future {
                Option(echoDao.findById(echoPossibilityId)).cata(
                    ep => channel ! GetEchoPossibilityResponse(msg, Right(ep)),
                    channel ! GetEchoPossibilityResponse(msg, Left(EchoPossibilityNotFound(echoPossibilityId)))
                )
            }.onFailure {
                case e =>
                    channel ! GetEchoPossibilityResponse(msg, Left(EchoException("Could not get echo possibility", e)))
                    log.error("Error processing %s" format msg, e)
            }



        case msg @ RecordEchoClick(echoClick, linkId, postId) =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            def error(e: Throwable) {
                channel ! RecordEchoClickResponse(msg, Left(EchoException("Could not record echo click", e)))
                log.error("Error processing %s" format msg, e)
            }

            //only use the postId if it looks like a valid uuid
            val id = Option(postId).flatMap { pi =>
                try {
                    Some(UUID.fromString(pi).toString)
                } catch {
                    case e => None
                }
            }.getOrElse(linkId)

            try {
                Option(echoDao.findByIdOrPostId(id)).cata(
                    echo => {
                        log.debug("Found {}", echo);
                        log.debug("Recording {}", echoClick);
                        channel ! RecordEchoClickResponse(msg, Right(echo))

                        //this really should be sent to another actor that has a durable mailbox...
                        Future {
                            val ec = determinePostId(echo, echoClick.copy(echoId = echo.id), if (linkId == echo.id) postId else linkId)
                            echoClickDao.insert(ec.copy(userAgent = Option(ec.userAgent).map(_.take(254)).orNull))
                            log.debug("Successfully recorded EchoClick {}", ec.id)


                            //start of really bad hack to filter some obviously bad clicks...
                            if (ec.userAgent == null || ec.userAgent.length < 1) {
                                throw FilteredException("Filtering null UserAgent for EchoClick %s" format ec.id, ec)
                            } else if (ec.echoedUserId == echo.echoedUserId) {
                                throw FilteredException("Filtering user clicking own echo EchoClick %s" format ec.id, ec)
                            } else if (filteredUserAgents.exists(ec.userAgent.contains(_))) {
                                throw FilteredException("Filtering robot EchoClick %s" format ec.id, ec)
                            }

                            Option(echoClickDao.findByEchoId(ec.echoId))
                                    .getOrElse(Collections.emptyList[EchoClick]())
                                    .filterNot(_.id == ec.id)
                                    .foreach { existing =>
                                if (!existing.filtered && existing.browserId == ec.browserId) {
                                    throw FilteredException("Filtering duplicate click from browser EchoClick %s" format ec.id, ec)
                                }
                                if (existing.ipAddress == ec.ipAddress && ((ec.createdOn.getTime - existing.createdOn.getTime) < 60000)) {
                                    throw FilteredException("Filtering fast click from same ipAddress EchoClick %s" format ec.id, ec)
                                }
                            }


                            (for {
                                echoMetrics <- Option(echoMetricsDao.findById(echo.echoMetricsId))
                                partnerSettings <- Option(partnerSettingsDao.findById(echo.partnerSettingsId))
                            } yield {
                                val clickedEcho = echoMetrics.clicked(partnerSettings)
                                echoMetricsDao.updateForClick(clickedEcho)
                                log.debug("Successfully updated click metrics for {}", clickedEcho.echoId)
                                true
                            }).orElse {
                                log.error("Failed to save echo click metrics for %s" format echo)
                                None
                            }
                        }.onFailure {
                            case e: FilteredException =>
                                log.debug(e.getMessage)
                                echoClickDao.updateFiltered(e.echoClick)
                            case e => log.error("Failed to save echo click %s for %s" format(echoClick, echo), e)
                        }
                    },
                    {
                        channel ! RecordEchoClickResponse(msg, Left(EchoNotFound(id)))
                        log.error("Did not find echo to record click - id {}, {}", id, echoClick)
                    })
            } catch {
                case e => error(e)
            }
    }

    def determinePostId(echo: Echo, echoClick: EchoClick, postId: String) =
        Option(postId) match {
            case Some(f) if echo.facebookPostId == f => echoClick.copy(facebookPostId = postId)
            case Some(t) if echo.twitterStatusId == t => echoClick.copy(twitterStatusId = postId)
            case Some("1") => echoClick
            case Some(_) =>
                log.warning("Invalid post id {} for {}", id, echo)
                echoClick
            case None =>
                echoClick
        }

}

private case class FilteredException(message: String, echoClick: EchoClick) extends EchoedException(message)

