package com.echoed.chamber.services.partner.shopify

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.shopify._
import akka.actor.{Channel, Actor}
import com.shopify.api.resources.{Order => SO, Product => SP}
import collection.JavaConversions
import com.echoed.chamber.dao._
import com.echoed.chamber.services.image.ImageService
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.Encrypter
import com.echoed.chamber.services.partner._
import java.util.{Date, HashMap}
import com.echoed.chamber.domain.Partner


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


    private val logger = LoggerFactory.getLogger(classOf[ShopifyPartnerServiceActor])

    self.id = "ShopifyPartnerService:%s" format shopifyPartner.id

    override def receive = shopifyPartnerReceive.orElse(super.receive)

    private def shopifyPartnerReceive: Receive = {
        case msg: GetShopifyPartner => self.channel ! GetShopifyPartnerResponse(msg, Right(shopifyPartner))

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
            val channel: Channel[RequestEchoResponse] = self.channel

            logger.debug("Received {}", msg)

            def error(e: Throwable) = e match {
                case pe: PartnerException => channel ! RequestEchoResponse(msg, Left(pe))
                case _ => channel ! RequestEchoResponse(msg, Left(PartnerException("Error requesting echo", e)))
            }

            try {
                val orderId = Integer.parseInt(order);

                (me ? GetOrderFull(orderId)).mapTo[GetOrderFullResponse].onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case GetOrderFullResponse(_, Left(e)) => error(e)
                        case GetOrderFullResponse(_, Right(order)) =>
                            val items = order.lineItems.map { li =>
                                val ei = new EchoItem()
                                ei.productId = li.productId
                                ei.productName = li.product.title
                                ei.category = li.product.category
                                ei.brand = shopifyPartner.name
                                ei.price = li.price.toFloat
                                ei.imageUrl = li.product.imageSrc
                                ei.landingPageUrl = "http://%s/products/%s" format(shopifyPartner.domain, li.product.handle)
                                ei.description = li.product.description
                                ei
                            }

                            val echoRequest: EchoRequest = new EchoRequest()
                            echoRequest.items = items
                            echoRequest.customerId = order.customerId
                            echoRequest.orderId = order.orderId
                            echoRequest.boughtOn = new Date
                            logger.debug("Echo Request: {}", echoRequest)

                            channel ! RequestEchoResponse(msg, Right(requestEcho(
                                echoRequest,
                                browserId,
                                ipAddress,
                                userAgent,
                                referrerUrl,
                                echoClickId)))
                    }))
            } catch {
                case e: InvalidEchoRequest => channel ! RequestEchoResponse(msg, Left(e))
                case e: PartnerNotActive => channel ! RequestEchoResponse(msg, Left(e))
                case e =>
                    logger.error("Error processing %s" format e, msg)
                    channel ! RequestEchoResponse(msg, Left(PartnerException("Error during echo request", e)))
            }


        case msg @ GetOrder(orderId) =>
            val channel: Channel[GetOrderResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetOrderResponse(msg, Left(ShopifyPartnerException("Error getting order")))
            }

            try {
                logger.debug("Fetching order {} for Shopify partner {}", orderId, shopifyPartner.name)
                shopifyAccess.fetchOrder(shopifyPartner.shopifyDomain, shopifyPartner.password, orderId).onComplete(_.value.get.fold(
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
            val channel: Channel[GetProductsResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetProductsResponse(msg, Left(ShopifyPartnerException("Error getting products")))
            }

            try {
                logger.debug("Fetching products for Shopify partner %s", shopifyPartner.name)
                shopifyAccess.fetchProducts(shopifyPartner.shopifyDomain, shopifyPartner.password).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case FetchProductsResponse(_, Left(e)) => error(e)
                        case FetchProductsResponse(_, Right(products)) =>
                            logger.debug("Received Products: {}", products)
                            channel ! GetProductsResponse(msg, Right(products))
                    }))
            } catch {
                case e => error(e)
            }


        case msg @ GetOrderFull(orderId) =>
            val me = self
            val channel: Channel[GetOrderFullResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetOrderFullResponse(msg, Left(ShopifyPartnerException("Error Getting Order Full")))
            }

            try {

                val order = (me ? GetOrder(orderId)).map(_ match {
                    case GetOrderResponse(_, Left(e)) => error(e)
                    case GetOrderResponse(_, Right(o)) => o
                }).map(_.asInstanceOf[SO])

                val products = (me ? GetProducts()).map(_ match {
                    case GetProductsResponse(_, Left(e)) => error(e)
                    case GetProductsResponse(_, Right(p)) => p
                }).map(_.asInstanceOf[List[SP]])

                (for {
                    o <- order
                    p <- products
                } yield {

                    val m = new HashMap[String, ShopifyProduct]
                    val sp = p.map {
                        new ShopifyProduct(_)
                    }

                    sp map {
                        t => m.put(t.id, t)
                    }

                    val lineItems = (JavaConversions.asScalaBuffer(o.getLineItems).map {
                        li => new ShopifyLineItem(li, m.get(li.getProductId.toString))
                    }).toList

                    val shopifyOrderFull = new ShopifyOrderFull(o, shopifyPartner, lineItems)

                    logger.debug("Responding with Shopify Orders: {}", shopifyOrderFull)
                    channel ! GetOrderFullResponse(msg, Right(shopifyOrderFull))
                })
            } catch {
                case e => error(e)
            }


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
