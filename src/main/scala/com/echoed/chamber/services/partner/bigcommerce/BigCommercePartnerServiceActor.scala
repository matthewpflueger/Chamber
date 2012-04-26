package com.echoed.chamber.services.partner.bigcommerce

import org.slf4j.LoggerFactory
import akka.actor.Channel
import com.echoed.chamber.dao._
import com.echoed.chamber.services.image.ImageService
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.Encrypter
import com.echoed.chamber.services.partner._
import com.echoed.chamber.domain.Partner
import com.echoed.chamber.domain.bigcommerce.BigCommercePartner


class BigCommercePartnerServiceActor(
            var bigCommercePartner: BigCommercePartner,
            partner: Partner,
            bigCommerceAccess: BigCommerceAccess,
            bigCommercePartnerDao: BigCommercePartnerDao,
            partnerDao: PartnerDao,
            partnerSettingsDao: PartnerSettingsDao,
            echoDao: EchoDao,
            echoMetricsDao: EchoMetricsDao,
            imageDao: ImageDao,
            imageService: ImageService,
            transactionTemplate: TransactionTemplate,
            encrypter: Encrypter) extends PartnerServiceActor(
        partner,
        partnerDao,
        partnerSettingsDao,
        echoDao,
        echoMetricsDao,
        imageDao,
        imageService,
        transactionTemplate,
        encrypter) {


    private val logger = LoggerFactory.getLogger(classOf[BigCommercePartnerServiceActor])

    self.id = "BigCommercePartnerService:%s" format bigCommercePartner.id

    override def receive = bigCommercePartnerReceive.orElse(super.receive)

    private def bigCommercePartnerReceive: Receive = {
        case msg @ RequestEcho(
                partnerId,
                order,
                browserId,
                ipAddress,
                userAgent,
                referrerUrl,
                echoedUserId,
                echoClickId,
                view) =>

            val channel: Channel[RequestEchoResponse] = self.channel

            logger.debug("Received {}", msg)

            def error(e: Throwable) = e match {
                case pe: PartnerException => channel ! RequestEchoResponse(msg, Left(pe))
                case _ => channel ! RequestEchoResponse(msg, Left(PartnerException("Error requesting echo", e)))
            }

            try {
                val orderId = java.lang.Long.parseLong(order)
                bigCommerceAccess.fetchOrder(bigCommercePartner.credentials, orderId).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case FetchOrderResponse(_, Left(e)) => error(e)
                        case FetchOrderResponse(_, Right(echoRequest)) =>
                            logger.debug("Received BigCommerce echo request {}", echoRequest)
                            channel ! RequestEchoResponse(msg, Right(requestEcho(
                                echoRequest.copy(items = echoRequest.items.map { i => i.copy(
                                    brand = Option(i.brand).getOrElse(partner.name),
                                    landingPageUrl = "%s%s" format(bigCommercePartner.storeUrl, i.landingPageUrl),
                                    imageUrl = "%s/product_images/%s" format(bigCommercePartner.storeUrl, i.imageUrl)
                                )}),
                                browserId,
                                ipAddress,
                                userAgent,
                                referrerUrl,
                                echoClickId)))
                    }))
            } catch { case e => error(e) }

    }

}
