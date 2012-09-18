package com.echoed.chamber.services.partner.magentogo

import com.echoed.chamber.dao._
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.Encrypter
import com.echoed.chamber.services.partner._
import partner.magentogo.MagentoGoPartnerDao
import partner.{PartnerDao, PartnerSettingsDao}
import com.echoed.chamber.services.MessageProcessor
import java.util.{List => JList}
import akka.actor.{ActorRef, ActorContext}
import akka.pattern._
import akka.util.Timeout


class MagentoGoPartnerService(
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
            filteredUserAgents: JList[String],
            magentoGoPartnerDao: MagentoGoPartnerDao,
            magentoGoAccessCreator: ActorContext => ActorRef,
            implicit val timeout: Timeout = Timeout(20000)) extends PartnerService(
        mp,
        partnerId,
        partnerDao,
        partnerSettingsDao,
        echoDao,
        echoClickDao,
        echoMetricsDao,
        imageDao,
        transactionTemplate,
        encrypter,
        filteredUserAgents) {

    private var magentoGoPartner = Option(magentoGoPartnerDao.findByPartnerId(partnerId)).get
    private val magentoGoAccess = magentoGoAccessCreator(context)

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
                (magentoGoAccess ? FetchOrder(magentoGoPartner.credentials, orderId)).onComplete(_.fold(
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
