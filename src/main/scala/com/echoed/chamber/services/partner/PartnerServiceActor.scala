package com.echoed.chamber.services.partner

import akka.actor.{ActorLogging, Actor}
import com.echoed.util.{ScalaObjectMapper, Encrypter}
import com.echoed.chamber.domain.views.EchoPossibilityView
import java.util.Date
import akka.dispatch.Future
import scalaz._
import Scalaz._
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.TransactionUtils._
import org.springframework.transaction.TransactionStatus
import com.echoed.chamber.dao._
import com.echoed.chamber.domain._
import com.echoed.chamber.services.image.{ProcessImageResponse, ImageService}
import java.util.concurrent.atomic.AtomicInteger
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import akka.event.Logging


class PartnerServiceActor(
        partner: Partner,
        partnerDao: PartnerDao,
        partnerSettingsDao: PartnerSettingsDao,
        echoDao: EchoDao,
        echoMetricsDao: EchoMetricsDao,
        imageDao: ImageDao,
        imageService: ImageService,
        transactionTemplate: TransactionTemplate,
        encrypter: Encrypter) extends Actor with ActorLogging {

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
                            imageService.processImage(ec.image).onComplete(_.fold(
                                e => log.error("Unexpected error processing image for echo {}: {}", ec.id, e),
                                _ match {
                                    case ProcessImageResponse(_, Left(e)) => log.error("Error processing image for echo {}: {}", ec.id, e)
                                    case ProcessImageResponse(_, Right(image)) => log.debug("Successfully processed image for echo {}", ec.id)
                                }
                            ))
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
        else new EchoPossibilityView(echoes, partner, partnerSettings)
    }


    def receive = {

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
                        classOf[EchoRequest]) //new TypeReference[EchoRequest]() {})

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
    }
}





