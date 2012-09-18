package com.echoed.chamber.services.partner.shopify

import com.echoed.util.TransactionUtils._
import com.echoed.chamber.domain.partner.shopify._
import collection.mutable.{Map => MMap}
import com.echoed.chamber.dao._
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.Encrypter
import com.echoed.chamber.services.partner._
import java.util.{Date, List => JList}
import akka.dispatch.Future
import partner.shopify.ShopifyPartnerDao
import partner.{PartnerSettingsDao, PartnerDao}
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import org.springframework.transaction.TransactionStatus
import com.echoed.chamber.services.MessageProcessor
import akka.actor.{ActorContext, ActorRef}


class ShopifyPartnerService(
        mp: MessageProcessor,
        partnerId: String,
        shopifyAccessCreator: ActorContext => ActorRef,
        shopifyPartnerDao: ShopifyPartnerDao,
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

    private var shopifyPartner = Option(shopifyPartnerDao.findByPartnerId(partnerId)).get
    private val shopifyAccess = shopifyAccessCreator(context)

    private implicit val timeout = Timeout(20 seconds)

    override def handle = shopifyPartnerHandle.orElse(super.handle)

    private def shopifyPartnerHandle: Receive = {
        case msg: GetShopifyPartner => sender ! GetShopifyPartnerResponse(msg, Right(shopifyPartner))

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

            val me = self
            val channel = context.sender

            log.debug("Received {}", msg)

            def error(e: Throwable) = e match {
                case pe: PartnerException => channel ! RequestEchoResponse(msg, Left(pe))
                case _ => channel ! RequestEchoResponse(msg, Left(PartnerException("Error requesting echo", e)))
            }

            try {
                val orderId = Integer.parseInt(order)

                (me ? GetOrderFull(orderId)).mapTo[GetOrderFullResponse].onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetOrderFullResponse(_, Left(e)) => error(e)
                        case GetOrderFullResponse(_, Right(order)) =>
                            log.debug("Creating {} EchoItems from Shopify order {}", order.lineItems.size, order.orderId)

                            val items = order.lineItems.map { li =>
                                EchoItem(
                                        productId = li.productId,
                                        productName = li.product.title,
                                        category = li.product.category,
                                        brand = shopifyPartner.name,
                                        price = li.price.toFloat,
                                        imageUrl = li.product.imageSrc,
                                        landingPageUrl = "http://%s/products/%s" format(shopifyPartner.domain, li.product.handle),
                                        description = li.product.description)
                            }

                            channel ! RequestEchoResponse(msg, Right(requestEcho(
                                    EchoRequest(order.orderId, order.customerId, new Date(), items),
                                    browserId,
                                    ipAddress,
                                    userAgent,
                                    referrerUrl,
                                    echoClickId,
                                    view)))
                    }))
            } catch {
                case e: InvalidEchoRequest => channel ! RequestEchoResponse(msg, Left(e))
                case e: PartnerNotActive => channel ! RequestEchoResponse(msg, Left(e))
                case e =>
                    log.error("Error processing {}: {}", e, msg)
                    channel ! RequestEchoResponse(msg, Left(PartnerException("Error during echo request", e)))
            }


        case msg @ GetOrder(orderId) =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetOrderResponse(msg, Left(ShopifyPartnerException("Error getting order")))
            }

            try {
                log.debug("Fetching order {} for Shopify partner {}", orderId, shopifyPartner.name)
                (shopifyAccess ? FetchOrder(shopifyPartner.shopifyDomain, shopifyPartner.password, orderId)).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchOrderResponse(_, Left(e)) => error(e)
                        case FetchOrderResponse(_, Right(order)) =>
                            log.debug("Received order {} for Shopify partner {}", order, shopifyPartner.name)
                            channel ! GetOrderResponse(msg, Right(order))
                    }))
            } catch {
                case e => error(e)
            }


        case msg @ GetProducts() =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetProductsResponse(msg, Left(ShopifyPartnerException("Error getting products")))
            }

            try {
                log.debug("Fetching products for Shopify partner %s", shopifyPartner.name)
                (shopifyAccess ? FetchProducts(shopifyPartner.shopifyDomain, shopifyPartner.password)).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchProductsResponse(_, Left(e)) => error(e)
                        case FetchProductsResponse(_, Right(products)) =>
                            log.debug("Received {} products", products.length)
                            channel ! GetProductsResponse(msg, Right(products))
                    }))
            } catch {
                case e => error(e)
            }


        case msg @ GetOrderFull(orderId) =>
            val me = self
            val channel = context.sender
            implicit val ec = context.dispatcher

            def error(e: Throwable) {
                log.error("Received error fetching Shopify order {} for {}", orderId, partner.name)
                 e match {
                    case spe: ShopifyPartnerException => channel ! GetOrderFullResponse(msg, Left(spe))
                    case _ => channel ! GetOrderFullResponse(
                            msg,
                            Left(ShopifyPartnerException("Error fetching Shopify order %s for %s" format(orderId, partner.name), e)))
                }
            }

            (me ? GetOrder(orderId)).onComplete(_.fold(
                e => error(e),
                _ match {
                    case GetOrderResponse(_, Left(e)) => error(e)
                    case GetOrderResponse(_, Right(o)) =>
                        log.debug("Successfully fetched Shopify order {} for {}", o.id, partner.name)

                        val liMap = MMap[String, LineItem]()
                        val productList = Future.sequence(o.lineItems.toList.map { li =>
                            log.debug("Fetching Shopify product {} for order {}", li.productId, o.id)
                            liMap(li.productId) = li
                            (shopifyAccess ? FetchProduct(shopifyPartner.shopifyDomain, shopifyPartner.password, li.productId)).mapTo[FetchProductResponse]
                        })

                        productList.onComplete(_.fold(
                            e => error(e),
                            resList => {
                                val shopifyLineItems = resList
                                    .filter(_.value.isRight)
                                    .map(res => new ShopifyProduct(res.resultOrException))
                                    .map(p => new ShopifyLineItem(liMap(p.id), p))
                                    .toList
                                log.debug("Successfully fetched {} products for order {}", shopifyLineItems.length, o.id)
                                channel ! GetOrderFullResponse(msg, Right(new ShopifyOrderFull(
                                            o,
                                            shopifyPartner,
                                            shopifyLineItems)))
                            }))
                }))


        case msg @ Update(sp) =>
            log.debug("Updating Shopify partner {}", sp.name)
            require(sp.shopifyDomain == this.shopifyPartner.shopifyDomain, "Trying to update Shopify partner %s with wrong data %s" format(shopifyPartner, sp))
            shopifyPartner = shopifyPartner.copy(
                name = sp.name,
                email = sp.email,
                phone = sp.phone,
                city = sp.city,
                country = sp.country,
                zip = sp.zip,
                password = sp.password,
                domain = sp.domain)
            partner = partner.copy(
                name = sp.name,
                domain = sp.domain,
                phone = sp.phone)

            transactionTemplate.execute({status: TransactionStatus =>
                partnerDao.update(partner)
                shopifyPartnerDao.update(shopifyPartner)
            })
            log.debug("Successfully updated Shopify partner {}", sp.name)

    }

}
