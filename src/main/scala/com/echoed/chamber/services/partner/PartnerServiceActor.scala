package com.echoed.chamber.services.partner

import org.slf4j.LoggerFactory
import akka.actor.{Channel, Actor}
import com.echoed.util.{ScalaObjectMapper, Encrypter}
import org.codehaus.jackson.`type`.TypeReference
import com.echoed.chamber.domain.views.EchoPossibilityView
import scala.reflect.BeanProperty
import java.util.Date
import akka.dispatch.Future
import scalaz._
import Scalaz._
import com.echoed.chamber.domain.{EchoMetrics, Echo, RetailerSettings, Retailer}
import com.echoed.chamber.dao.{EchoMetricsDao, EchoDao, RetailerSettingsDao, RetailerDao}
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.TransactionUtils._
import org.springframework.transaction.TransactionStatus

class PartnerServiceActor(
        partner: Retailer,
        partnerDao: RetailerDao,
        partnerSettingsDao: RetailerSettingsDao,
        echoDao: EchoDao,
        echoMetricsDao: EchoMetricsDao,
        transactionTemplate: TransactionTemplate,
        encrypter: Encrypter) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerServiceActor])

    self.id = "Partner:%s" format partner.id

    def receive = {

        case msg @ GetPartner() =>
            val channel: Channel[GetPartnerResponse] = self.channel
            channel ! GetPartnerResponse(msg, Right(partner))

        case msg @ RequestEcho(request, ipAddress, echoedUserId, echoClickId) =>
            val channel: Channel[RequestEchoResponse] = self.channel

            logger.debug("Received {}", msg)
            try {
                val decryptedRequest = encrypter.decrypt(request, partner.secret)
                logger.debug("Partner {} received echo request {}", partner.name, decryptedRequest)

                val echoRequest: EchoRequest = new ScalaObjectMapper().readValue(
                        decryptedRequest,
                        new TypeReference[EchoRequest]() {})

                val partnerSettings = Option(partnerSettingsDao.findByActiveOn(partner.id, new Date))
                        .getOrElse(throw new PartnerNotActive(partner.id))

                val echoes = echoRequest.items.map { i =>
                    Echo.make(
                            partner.id,
                            echoRequest.customerId,
                            i.productId,
                            echoRequest.boughtOn,
                            "request",
                            echoRequest.orderId,
                            i.price,
                            i.imageUrl,
                            i.landingPageUrl,
                            i.productName,
                            i.category,
                            i.brand,
                            i.description.take(1023),
                            echoClickId.orNull,
                            partnerSettings.id)
                }.filter { ep =>
                    try {
                        val echoMetrics = new EchoMetrics(ep, partnerSettings)
                        transactionTemplate.execute({status: TransactionStatus =>
                            echoMetricsDao.insert(echoMetrics)
                            echoDao.insert(ep.copy(echoMetricsId = echoMetrics.id))
                        })
                        true
                    } catch { case e => logger.error("Could not save %s" format ep, e); false }
                }

                if (echoes.isEmpty) {
                    channel ! RequestEchoResponse(msg, Left(InvalidEchoRequest()))
                } else {
                    channel ! RequestEchoResponse(
                            msg,
                            Right(new EchoPossibilityView(echoes, partner, partnerSettings)))
                }
            } catch {
                case e: PartnerNotActive =>
                    channel ! RequestEchoResponse(msg, Left(e))
                case e =>
                    logger.error("Error processing %s" format e, msg)
                    channel ! RequestEchoResponse(msg, Left(PartnerException("Error during echo request", e)))
            }

        case msg @ RequestShopifyEcho(order, ipAddress, echoedUserId, echoClickId) =>
            val channel: Channel[RequestShopifyEchoResponse] = self.channel

            logger.debug("Received {}", msg)
            try {
                val items = order.lineItems.map { li => 
                    val ei = new EchoItem()
                    ei.productId = li.productId
                    ei.productName = li.product.title
                    ei.category = li.product.category
                    ei.brand = partner.name
                    ei.price = li.price.toFloat
                    ei.imageUrl = li.product.imageSrc
                    ei.landingPageUrl = li.product.imageSrc
                    ei.description = li.product.description
                    ei
                }

                val partnerSettings = Option(partnerSettingsDao.findByActiveOn(partner.id, new Date))
                    .getOrElse(throw new PartnerNotActive(partner.id))


                val echoRequest: EchoRequest = new EchoRequest()
                echoRequest.items = items
                echoRequest.customerId = order.customerId
                echoRequest.orderId = order.orderId
                echoRequest.boughtOn = new Date
                logger.debug("Echo Request: {}", echoRequest)

                val echoes = echoRequest.items.map { i=>
                    Echo.make(
                        partner.id,
                        echoRequest.customerId,
                        i.productId,
                        echoRequest.boughtOn,
                        "request",
                        echoRequest.orderId,
                        i.price,
                        i.imageUrl,
                        i.landingPageUrl,
                        i.productName,
                        i.category,
                        i.brand,
                        i.description.take(1023),
                        echoClickId.orNull,
                        partnerSettings.id)

                }.filter { ep =>
                    try {
                        val echoMetrics = new EchoMetrics(ep, partnerSettings)
                        transactionTemplate.execute({status: TransactionStatus =>
                            echoMetricsDao.insert(echoMetrics)
                            echoDao.insert(ep.copy(echoMetricsId = echoMetrics.id))
                        })
                        true
                    } catch { case e => logger.error("Could not save %s" format ep, e ); false }
                    
                }
                
                if (echoes.isEmpty) {
                    channel ! RequestShopifyEchoResponse(msg, Left(InvalidEchoRequest()))
                } else {
                    channel ! RequestShopifyEchoResponse(
                            msg,
                            Right(new EchoPossibilityView(echoes, partner, partnerSettings))
                    )
                }

            } catch {
                case e =>
                    logger.error("Error processing %s" format e, msg)
                    channel ! RequestShopifyEchoResponse(msg, Left(PartnerException("Error during echo request", e)))
            }

        case msg @ RecordEchoStep(echoId, step, ipAddress, echoedUserId, echoClickId) =>
            val channel: Channel[RecordEchoStepResponse] = self.channel

            logger.debug("Processing {}", msg)

            Future {
                echoDao.findById(echoId)
            }.onComplete(_.value.get.fold(
                e => channel ! RecordEchoStepResponse(msg, Left(PartnerException("Error retrieving echo %s" format echoId, e))),
                ep => {
                    val partnerSettings = partnerSettingsDao.findById(ep.retailerSettingsId)
                    val epv = new EchoPossibilityView(ep, partner, partnerSettings)
                    if (ep.isEchoed) {
                        channel ! RecordEchoStepResponse(msg, Left(EchoExists(epv)))
                    } else {
                        channel ! RecordEchoStepResponse(msg, Right(epv))
                        echoDao.updateForStep(ep.copy(step = "%s,%s" format(ep.step, step)))
                        logger.debug("Recorded step {} for echo {}", step, ep.id)
                    }
            })).onException {
                case e =>
                    channel ! RecordEchoStepResponse(msg, Left(PartnerException("Unexpected error", e)))
                    logger.error("Error processing %s" format msg, e)
            }
    }

}

class EchoRequest {
    @BeanProperty var customerId: String = _
    @BeanProperty var orderId: String = _
    @BeanProperty var boughtOn: Date = _
    @BeanProperty var items: List[EchoItem] = _
}

class EchoItem {
    @BeanProperty var productId: String = _
    @BeanProperty var productName: String = _
    @BeanProperty var category: String = _
    @BeanProperty var brand: String = _
    @BeanProperty var price: Float = 0
    @BeanProperty var imageUrl: String = _
    @BeanProperty var landingPageUrl: String = _
    @BeanProperty var description: String = _
}
