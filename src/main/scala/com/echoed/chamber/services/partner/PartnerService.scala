package com.echoed.chamber.services.partner

import com.echoed.util.{ScalaObjectMapper, Encrypter}
import com.echoed.chamber.domain.views.EchoPossibilityView
import java.util.{Collections, Date}
import akka.dispatch.Future
import scalaz._
import Scalaz._
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.TransactionUtils._
import org.springframework.transaction.TransactionStatus
import com.echoed.chamber.dao._
import com.echoed.chamber.domain._
import com.echoed.chamber.services.image.{ProcessImage}
import java.util.concurrent.atomic.AtomicInteger
import java.util.{List => JList}
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import com.echoed.chamber.services.{MessageProcessor, EchoedService}
import scala.collection.JavaConversions._


class PartnerService(
        mp: MessageProcessor,
        partnerId: String,
        partnerDao: PartnerDao,
        partnerSettingsDao: PartnerSettingsDao,
        echoDao: EchoDao,
        echoClickDao: EchoClickDao,
        echoMetricsDao: EchoMetricsDao,
        imageDao: ImageDao,
        transactionTemplate: TransactionTemplate,
        encrypter: Encrypter,
        filteredUserAgents: JList[String]) extends EchoedService {

    protected var partner = Option(partnerDao.findById(partnerId)).get

    val viewCounter: AtomicInteger = new AtomicInteger(0)

    protected def requestEcho(
            echoRequest: EchoRequest,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String,
            echoClickId: Option[String] = None,
            view: Option[String] = None) = {

        val partnerSettings = Option(partnerSettingsDao.findByActiveOn(partner.id, new Date))
                .getOrElse(Option(partnerSettingsDao.findInactive(partner.id,new Date))
                    .getOrElse(throw new PartnerNotActive(partner)))

        val echoes = echoRequest.items.map { i =>
            Echo.make(
                partnerId = partner.id,
                customerId = echoRequest.customerId,
                productId = i.productId,
                boughtOn = echoRequest.boughtOn,
                step = "request",
                orderId = echoRequest.orderId,
                price = i.price,
                imageUrl = i.imageUrl,
                landingPageUrl = i.landingPageUrl,
                productName = i.productName,
                category = i.category,
                brand = i.brand,
                description = i.description,
                echoClickId = echoClickId.orNull,
                browserId = browserId,
                ipAddress = ipAddress,
                userAgent = userAgent,
                referrerUrl = referrerUrl,
                partnerSettingsId = partnerSettings.id,
                view = view.orNull)
        }.map { ec =>
            try {
                //check for existing echo in the case of a page refresh we do not want to bomb out...
                Option(echoDao.findByEchoPossibilityId(ec.echoPossibilityId)).getOrElse {
                    transactionTemplate.execute({status: TransactionStatus =>
                        val echoMetrics = new EchoMetrics(ec, partnerSettings)
                        val img = Option(imageDao.findByUrl(ec.image.url)).getOrElse {
                            log.debug("New image for processing {}", ec.image.url)
                            imageDao.insert(ec.image)
                            mp(ProcessImage(ec.image))
                            ec.image
                        }
                        echoMetricsDao.insert(echoMetrics)
                        val echo = ec.copy(echoMetricsId = echoMetrics.id, image = img)
                        echoDao.insert(echo)
                        echo
                    })
                }
            } catch { case e => log.error("Could not save {}: {}", ec, e) }
        }.filter(_.isInstanceOf[Echo]).map(_.asInstanceOf[Echo])

        if (echoes.isEmpty) throw new InvalidEchoRequest()
        else EchoPossibilityView(echoes, partner, partnerSettings)
    }


    def handle = {

        case msg: FetchPartner =>
            log.debug("Fetching Partner")
            val channel = context.sender
            channel ! FetchPartnerResponse(msg, Right(partner))

        case msg: GetPartner =>
            val channel = context.sender
            channel ! GetPartnerResponse(msg, Right(partner))

        case msg @ RequestEcho(
                partnerId,
                request,
                browserId,
                ipAddress,
                userAgent,
                referrerUrl,
                echoedUserId,
                echoClickId,
                view) =>
            val channel = context.sender

            log.debug("Received {}", msg)
            try {
                val decryptedRequest = encrypter.decrypt(request, partner.secret)
                log.debug("Partner {} received echo request {}", partner.name, decryptedRequest)

                val echoRequest: EchoRequest = new ScalaObjectMapper().readValue(
                        decryptedRequest,
                        classOf[EchoRequest])

                channel ! RequestEchoResponse(msg, Right(requestEcho(
                        echoRequest,
                        browserId,
                        ipAddress,
                        userAgent,
                        referrerUrl,
                        echoClickId,
                        view)))
            } catch {
                case e: InvalidEchoRequest => channel ! RequestEchoResponse(msg, Left(e))
                case e: PartnerNotActive => channel ! RequestEchoResponse(msg, Left(e))
                case e =>
                    log.error("Error processing {}: {}", msg, e)
                    channel ! RequestEchoResponse(msg, Left(PartnerException("Error during echo request", e)))
            }

        case msg @ GetEcho(echoId) =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            log.debug("Processing {}", msg)
            Future {
                echoDao.findByIdOrPostId(echoId)
            }.onComplete(_.fold(
                e => channel ! GetEchoResponse(msg, Left(PartnerException("Error retrieving echo %s" format echoId, e))),
                ep => {
                    val partnerSettings = partnerSettingsDao.findById(ep.partnerSettingsId)
                    val epv = new EchoPossibilityView(ep, partner, partnerSettings)
                    channel ! GetEchoResponse(msg, Right(epv))
                    log.debug("Returned EchoPossibility View: {}", epv)
            })).onFailure {
                case e =>
                    channel ! GetEchoResponse(msg, Left(PartnerException("Unexpected error", e)))
                    log.error("Error processing {}: {}", msg, e)
            }

        case msg @ RecordEchoStep(echoId, step, echoedUserId, echoClickId) =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            log.debug("Processing {}", msg)

            Future {
                echoDao.findByIdOrPostId(echoId)
            }.onComplete(_.fold(
                e => channel ! RecordEchoStepResponse(msg, Left(PartnerException("Error retrieving echo %s" format echoId, e))),
                ep => {
                    val partnerSettings = partnerSettingsDao.findById(ep.partnerSettingsId)
                    val epv = new EchoPossibilityView(ep, partner, partnerSettings)
                    if (ep.isEchoed) {
                        channel ! RecordEchoStepResponse(msg, Left(EchoExists(epv)))
                    } else {
                        channel ! RecordEchoStepResponse(msg, Right(epv))
                        echoDao.updateForStep(ep.copy(step = ("%s,%s" format(ep.step, step)).takeRight(254)))
                        log.debug("Recorded step {} for echo {}", step, ep.id)
                    }
            })).onFailure {
                case e =>
                    channel ! RecordEchoStepResponse(msg, Left(PartnerException("Unexpected error", e)))
                    log.error("Error processing {}: {}", msg, e)
            }


        case msg: GetView =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            Future {
                Option(partnerSettingsDao.findByActiveOn(partner.id, new Date))
            }.onComplete(_.fold(
                e => channel ! GetViewResponse(msg, Left(PartnerException("Error retrieving partner settings for %s" format partner.id, e))),
                _.cata(
                    ps => {
                        val view = ps.viewsList(viewCounter.incrementAndGet() % ps.viewsList.length)
                        channel ! GetViewResponse(msg, Right(ViewDescription(view, Map(
                                        "pid" -> partner.id,
                                        "view" -> view,
                                        "partner" -> partner,
                                        "partnerSettings" -> ps,
                                        "maxPercentage" -> "%1.0f".format(ps.maxPercentage * 100)))))
                    },
                    channel ! GetViewResponse(msg, Left(PartnerNotActive(partner)))
            ))).onFailure {
                case e =>
                    channel ! GetViewResponse(msg, Left(PartnerException("Unexpected error", e)))
                    log.error("Error processing {}: {}", msg, e)
            }

        case msg @ RecordEchoClick(postId, echoClick) =>
            val channel = context.sender
            implicit val ec = context.dispatcher

            Option(echoDao.findByIdOrPostId(postId)).cata(
                echo => {
                    log.debug("Found {}", echo)
                    log.debug("Recording {}", echoClick)
                    channel ! RecordEchoClickResponse(msg, Right(new EchoPossibilityView(
                            echo,
                            partner,
                            partnerSettingsDao.findById(echo.partnerSettingsId))))

                    //this really should be sent to another actor that has a durable mailbox...
                    Future {
                        val ec = determinePostId(echo, echoClick.copy(echoId = echo.id), postId)
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
                    channel ! RecordEchoClickResponse(msg, Left(EchoNotFound(postId)))
                    log.error("Did not find echo to record click - id {}, {}", id, echoClick)
                })
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





