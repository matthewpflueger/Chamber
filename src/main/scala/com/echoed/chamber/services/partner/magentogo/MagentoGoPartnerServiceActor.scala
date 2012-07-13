package com.echoed.chamber.services.partner.magentogo

import org.slf4j.LoggerFactory
import com.echoed.chamber.dao._
import com.echoed.chamber.services.image.ImageService
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.Encrypter
import com.echoed.chamber.services.partner._

import com.echoed.chamber.domain.partner.magentogo.MagentoGoPartner
import partner.magentogo.MagentoGoPartnerDao
import partner.{PartnerDao, PartnerSettingsDao}
import com.echoed.chamber.domain.partner.Partner


class MagentoGoPartnerServiceActor(
            var magentoGoPartner: MagentoGoPartner,
            partner: Partner,
            magentoGoAccess: MagentoGoAccess,
            magentoGoPartnerDao: MagentoGoPartnerDao,
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


    override def handle = magentoGoPartnerHandle.orElse(super.handle)

    private def magentoGoPartnerHandle: Receive = {
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
                magentoGoAccess.fetchOrder(magentoGoPartner.credentials, orderId).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchOrderResponse(_, Left(e)) => error(e)
                        case FetchOrderResponse(_, Right(echoRequest)) =>
                            log.debug("Received MagentoGo echo request {}", echoRequest)
                            channel ! RequestEchoResponse(msg, Right(requestEcho(
                                echoRequest.copy(items = echoRequest.items.map { i => i.copy(
                                    landingPageUrl = "%s/%s" format(magentoGoPartner.storeUrl, i.landingPageUrl)
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
