package com.echoed.chamber.services.partner

import org.slf4j.LoggerFactory
import akka.actor.{Channel, Actor}
import com.echoed.util.{ScalaObjectMapper, Encrypter}
import org.codehaus.jackson.`type`.TypeReference
import com.echoed.chamber.domain.{RetailerSettings, EchoPossibility, Retailer}
import com.echoed.chamber.domain.views.EchoPossibilityView
import scala.reflect.BeanProperty
import java.util.Date
import akka.dispatch.Future
import com.echoed.chamber.dao.{EchoDao, RetailerSettingsDao, EchoPossibilityDao, RetailerDao}
import scalaz._
import Scalaz._


class PartnerServiceActor(
        partner: Retailer,
        partnerSettings: RetailerSettings,
        partnerDao: RetailerDao,
        partnerSettingsDao: RetailerSettingsDao,
        echoPossibilityDao: EchoPossibilityDao,
        echoDao: EchoDao,
        encrypter: Encrypter) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerServiceActor])

    self.id = "Partner:%s" format partner.id

    def receive = {
        case msg @ RequestEcho(request, ipAddress, echoedUserId, echoClickId) =>
            val channel: Channel[RequestEchoResponse] = self.channel

            logger.debug("Received {}", msg)
            try {
                val decryptedRequest = encrypter.decrypt(request, partner.secret)
                logger.debug("Partner {} received echo request {}", partner.name, decryptedRequest)

                val echoRequest: EchoRequest = new ScalaObjectMapper().readValue(
                        decryptedRequest,
                        new TypeReference[EchoRequest]() {})

                val echoPossibilities = echoRequest.items.map { i =>
                    new EchoPossibility(
                            partner.id,
                            echoRequest.customerId,
                            i.productId,
                            echoRequest.boughtOn,
                            "request",
                            echoRequest.orderId,
                            i.price,
                            i.imageUrl,
                            echoedUserId.orNull,
                            null, //echoId
                            i.landingPageUrl,
                            i.productName,
                            i.category,
                            i.brand,
                            i.description.take(1023),
                            echoClickId.orNull)
                }.filter { ep =>
                    try {
                        echoPossibilityDao.insertOrUpdate(ep)
                        true
                    } catch { case e => logger.error("Could not save %s" format ep, e); false }
                }

                if (echoPossibilities.isEmpty) {
                    channel ! RequestEchoResponse(msg, Left(InvalidEchoRequest()))
                } else {
                    channel ! RequestEchoResponse(
                            msg,
                            Right(new EchoPossibilityView(echoPossibilities, partner, partnerSettings)))
                }
            } catch {
                case e =>
                    logger.error("Error processing %s" format e, msg)
                    channel ! RequestEchoResponse(msg, Left(PartnerException("Error during echo request", e)))
            }


        case msg @ RecordEchoStep(echoId, step, ipAddress, echoedUserId, echoClickId) =>
            import com.echoed.chamber.services.echo.{RecordEchoPossibilityResponse => REPR}

            val channel: Channel[RecordEchoStepResponse] = self.channel

            logger.debug("Processing {}", msg)

            Future {
                echoPossibilityDao.findById(echoId)
            }.onComplete(_.value.get.fold(
                e => channel ! RecordEchoStepResponse(msg, Left(PartnerException("Error retrieving echo %s" format echoId, e))),
                ep => {
                    val epv = new EchoPossibilityView(ep, partner, partnerSettings)
                    Option(echoDao.findByEchoPossibilityId(echoId)).cata(
                        ec => channel ! RecordEchoStepResponse(msg, Left(EchoExists(epv.copy(echo = ec)))),
                        {
                            channel ! RecordEchoStepResponse(msg, Right(epv))
                            echoPossibilityDao.insertOrUpdate(ep.copy(
                                    step = "%s,%s" format(ep.step, step),
                                    echoedUserId = echoedUserId.getOrElse(ep.echoedUserId),
                                    echoClickId = echoClickId.getOrElse(ep.echoClickId)))
                            logger.debug("Recorded step {} for echo {}", step, ep.id)
                        })
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

