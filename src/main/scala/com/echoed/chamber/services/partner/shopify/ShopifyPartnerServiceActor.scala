package com.echoed.chamber.services.partner.shopify

import com.echoed.chamber.domain.partner.shopify._
import collection.mutable.{Map => MMap}
import collection.JavaConversions._
import com.echoed.chamber.dao._
import com.echoed.chamber.services.image.ImageService
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.Encrypter
import com.echoed.chamber.services.partner._
import java.util.Date
import com.echoed.chamber.domain.partner.Partner
import akka.dispatch.Future
import com.shopify.api.resources.LineItem
import partner.shopify.ShopifyPartnerDao
import partner.{PartnerSettingsDao, PartnerDao}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class ShopifyPartnerServiceActor(
        var shopifyPartner: ShopifyPartner,
        partner: Partner,
        shopifyAccess: ShopifyAccess,
        shopifyPartnerDao: ShopifyPartnerDao,
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


    private val logger = Logging(context.system, this)

    private implicit val timeout = Timeout(20 seconds)

    override def receive = shopifyPartnerReceive.orElse(super.receive)

    private def shopifyPartnerReceive: Receive = {
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

            logger.debug("Received {}", msg)

            def error(e: Throwable) = e match {
                case pe: PartnerException => channel ! RequestEchoResponse(msg, Left(pe))
                case _ => channel ! RequestEchoResponse(msg, Left(PartnerException("Error requesting echo", e)))
            }

            try {
                val orderId = Integer.parseInt(order);

                (me ? GetOrderFull(orderId)).mapTo[GetOrderFullResponse].onComplete(_.fold(
                    error(_),
                    _ match {
                        case GetOrderFullResponse(_, Left(e)) => error(e)
                        case GetOrderFullResponse(_, Right(order)) =>
                            logger.debug("Creating {} EchoItems from Shopify order {}", order.lineItems.size, order.orderId)

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
                    logger.error("Error processing %s" format e, msg)
                    channel ! RequestEchoResponse(msg, Left(PartnerException("Error during echo request", e)))
            }


        case msg @ GetOrder(orderId) =>
            val channel = context.sender

            def error(e: Throwable) {
                channel ! GetOrderResponse(msg, Left(ShopifyPartnerException("Error getting order")))
            }

            try {
                logger.debug("Fetching order {} for Shopify partner {}", orderId, shopifyPartner.name)
                shopifyAccess.fetchOrder(shopifyPartner.shopifyDomain, shopifyPartner.password, orderId).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchOrderResponse(_, Left(e)) => error(e)
                        case FetchOrderResponse(_, Right(order)) =>
                            logger.debug("Received order {} for Shopify partner {}", order, shopifyPartner.name)
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
                logger.debug("Fetching products for Shopify partner %s", shopifyPartner.name)
                shopifyAccess.fetchProducts(shopifyPartner.shopifyDomain, shopifyPartner.password).onComplete(_.fold(
                    error(_),
                    _ match {
                        case FetchProductsResponse(_, Left(e)) => error(e)
                        case FetchProductsResponse(_, Right(products)) =>
                            logger.debug("Received {} products", products.length)
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
                logger.error("Received error fetching Shopify order %s for %s" format(orderId, partner.name))
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
                        logger.debug("Successfully fetched Shopify order {} for {}", o.getId, partner.name)

                        val liMap = MMap[String, LineItem]()
                        val productList = Future.sequence(o.getLineItems.map { li =>
                            logger.debug("Fetching Shopify product {} for order {}", li.getProductId, o.getId)
                            liMap(li.getProductId.toString) = li
                            shopifyAccess.fetchProduct(shopifyPartner.shopifyDomain, shopifyPartner.password, li.getProductId)
                        })

                        productList.onComplete(_.fold(
                            e => error(e),
                            resList => {
                                val shopifyLineItems = resList
                                    .filter(_.value.isRight)
                                    .map(res => new ShopifyProduct(res.resultOrException))
                                    .map(p => new ShopifyLineItem(liMap(p.id), p))
                                    .toList
                                logger.debug("Successfully fetched {} products for order {}", shopifyLineItems.length, o.getId)
                                channel ! GetOrderFullResponse(msg, Right(new ShopifyOrderFull(
                                            o,
                                            shopifyPartner,
                                            shopifyLineItems)))
                            }))
                }))


        case msg @ Update(sp) =>
            require(sp.domain == this.shopifyPartner.domain, "Trying to update Shopify partner %s with wrong data %s" format(shopifyPartner, sp))
            shopifyPartner = shopifyPartner.copy(
                name = sp.name,
                email = sp.email,
                phone = sp.phone,
                city = sp.city,
                country = sp.country,
                zip = sp.zip,
                password = sp.password)
            shopifyPartnerDao.update(shopifyPartner)
    }

}
