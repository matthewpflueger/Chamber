package com.echoed.chamber.services.shopify

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.dao.ShopifyUserDao
import akka.actor.{Channel, Actor}
import shopify.ShopifyUser


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
        case msg: GetShopifyUser =>
            val channel: Channel[GetShopifyUserResponse] = self.channel
            channel ! GetShopifyUserResponse(msg, Right(shopifyUser))
            
        case _ =>
            self.channel ! None
    }

}
