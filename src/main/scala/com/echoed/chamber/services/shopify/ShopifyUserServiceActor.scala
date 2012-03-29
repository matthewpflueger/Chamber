package com.echoed.chamber.services.shopify

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.shopify._
import com.echoed.chamber.dao.ShopifyUserDao
import akka.actor.{Channel, Actor}
import shopify.ShopifyUser
import com.shopify.api.resources.{ Order => SO, Product => SP }
import collection.JavaConversions
import java.util.{HashMap}

class ShopifyUserServiceActor(
        shopifyAccess: ShopifyAccess,
        shopifyUserDao: ShopifyUserDao,
        shopifyUser: ShopifyUser) extends Actor {


    private val logger = LoggerFactory.getLogger(classOf[ShopifyUserServiceActor])

    self.id = "ShopifyUserService:%s" format shopifyUser.id

    def receive = {
        case msg @ GetOrder(orderId) =>
            val channel: Channel[GetOrderResponse] = self.channel

            def error(e: Throwable) {
                channel ! GetOrderResponse(msg , Left(ShopifyException("Error Getting Order")))
            }

            try {
                logger.debug("Fetching Order: {}", orderId)
                shopifyAccess.fetchOrder(shopifyUser.domain, shopifyUser.password, orderId).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case FetchOrderResponse(_ , Left(e)) => error(e)
                        case FetchOrderResponse(_ , Right(order)) =>
                            logger.debug("Received Order: {}", order)
                            channel ! GetOrderResponse(msg, Right(order))
                    }))
            } catch {
                case e => error(e)
            }

        case msg @ GetProducts() =>
            val channel: Channel[GetProductsResponse] = self.channel
            
            def error(e: Throwable) {
                channel ! GetProductsResponse(msg, Left(ShopifyException("Error Getting Products")))
            }
            
            try {
                logger.debug("Fetching Products")
                shopifyAccess.fetchProducts(shopifyUser.domain, shopifyUser.password).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case FetchProductsResponse(_, Left(e)) => error(e)
                        case FetchProductsResponse(_, Right(products)) =>
                            logger.debug("Received Products: {}", products)
                            channel ! GetProductsResponse(msg, Right(products))
                    }))
            }
            
        case msg @ GetOrderFull(orderId) =>
            val me = self
            val channel: Channel[GetOrderFullResponse] = self.channel
            
            def error(e: Throwable) {
                channel ! GetOrderFullResponse(msg, Left(ShopifyException("Error Getting Order Full")))
            }

            try {
                
                val order = (me ? GetOrder(orderId)).map(_ match {
                        case GetOrderResponse(_, Left(e)) => error(e)
                        case GetOrderResponse(_, Right(o)) =>o
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
                    val sp = p.map { new ShopifyProduct(_) }

                    sp map { t => m.put(t.id,t) }

                    val lineItems = (JavaConversions.asScalaBuffer(o.getLineItems).map {
                        li => new ShopifyLineItem(li, m.get(li.getProductId.toString))
                    }).toList

                    val shopifyOrderFull = new ShopifyOrderFull(o, shopifyUser, lineItems)

                    logger.debug("Responding with Shopify Orders: {}", shopifyOrderFull)
                    channel ! GetOrderFullResponse(msg, Right(shopifyOrderFull))
                })
            } catch {
                case e => error(e)
            }
            
            
        case msg: GetShopifyUser =>
            val channel: Channel[GetShopifyUserResponse] = self.channel
            channel ! GetShopifyUserResponse(msg, Right(shopifyUser))
            
        case _ =>
            self.channel ! None
    }

}
