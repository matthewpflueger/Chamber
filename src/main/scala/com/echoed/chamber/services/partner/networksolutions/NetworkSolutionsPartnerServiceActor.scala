package com.echoed.chamber.services.partner.networksolutions

import org.slf4j.LoggerFactory
import com.echoed.chamber.dao._
import com.echoed.chamber.services.image.ImageService
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.Encrypter
import com.echoed.chamber.services.partner._
import com.echoed.chamber.domain.partner.networksolutions.NetworkSolutionsPartner
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.dao.partner.networksolutions.NetworkSolutionsPartnerDao
import com.echoed.chamber.dao.partner.{PartnerSettingsDao, PartnerDao}
import akka.event.Logging


class NetworkSolutionsPartnerServiceActor(
        var networkSolutionsPartner: NetworkSolutionsPartner,
        partner: Partner,
        networkSolutionsAccess: NetworkSolutionsAccess,
        networkSolutionsPartnerDao: NetworkSolutionsPartnerDao,
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


    override def handle = networkSolutionsPartnerHandle.orElse(super.handle)

    private def networkSolutionsPartnerHandle: Receive = {
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
                val orderNumber = java.lang.Long.parseLong(order)

                networkSolutionsAccess.fetchOrder(networkSolutionsPartner.userToken, orderNumber).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchOrderResponse(_, Left(e)) => error(e)
                        case FetchOrderResponse(_, Right(echoRequest)) =>
                            channel ! RequestEchoResponse(msg, Right(requestEcho(
                                echoRequest.copy(items = echoRequest.items.map { i => i.copy(
                                    brand = Option(i.brand).getOrElse(partner.name),
                                    landingPageUrl = "%s/%s" format(networkSolutionsPartner.storeUrl, i.landingPageUrl),
                                    imageUrl = "%s/%s" format(networkSolutionsPartner.storeUrl, i.imageUrl)
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
