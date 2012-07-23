package com.echoed.chamber.services.partner.bigcommerce

import com.echoed.chamber.dao._
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.Encrypter
import com.echoed.chamber.services.partner._
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.partner.bigcommerce.BigCommercePartner
import partner.bigcommerce.BigCommercePartnerDao
import partner.{PartnerSettingsDao, PartnerDao}
import com.echoed.chamber.services.MessageProcessor
import java.util.{List => JList}


class BigCommercePartnerService (
            mp: MessageProcessor,
            var bigCommercePartner: BigCommercePartner,
            partner: Partner,
            bigCommercePartnerDao: BigCommercePartnerDao,
            partnerDao: PartnerDao,
            partnerSettingsDao: PartnerSettingsDao,
            echoDao: EchoDao,
            echoClickDao: EchoClickDao,
            echoMetricsDao: EchoMetricsDao,
            imageDao: ImageDao,
            transactionTemplate: TransactionTemplate,
            encrypter: Encrypter,
            filteredUserAgents: JList[String]) extends PartnerService(
        mp,
        partner,
        partnerDao,
        partnerSettingsDao,
        echoDao,
        echoClickDao,
        echoMetricsDao,
        imageDao,
        transactionTemplate,
        encrypter,
        filteredUserAgents) {


    override def handle = bigCommercePartnerHandle.orElse(super.handle)

    private def bigCommercePartnerHandle: Receive = {
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

            val channel = context.sender

            log.debug("Received {}", msg)

            def error(e: Throwable) = e match {
                case pe: PartnerException => channel ! RequestEchoResponse(msg, Left(pe))
                case _ => channel ! RequestEchoResponse(msg, Left(PartnerException("Error requesting echo", e)))
            }

            try {
                val orderId = java.lang.Long.parseLong(order)
                mp(FetchOrder(bigCommercePartner.credentials, orderId)).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchOrderResponse(_, Left(e)) => error(e)
                        case FetchOrderResponse(_, Right(echoRequest)) =>
                            log.debug("Received BigCommerce echo request {}", echoRequest)
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
                                echoClickId,
                                view)))
                    }))
            } catch { case e => error(e) }

    }

}
