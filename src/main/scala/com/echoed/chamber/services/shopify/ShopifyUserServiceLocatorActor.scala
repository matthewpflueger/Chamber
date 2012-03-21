package com.echoed.chamber.services.shopify

import scala.collection.mutable.{ConcurrentMap, WeakHashMap}

import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import akka.actor._

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/16/12
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */

class ShopifyUserServiceLocatorActor extends Actor{

    private val logger = LoggerFactory.getLogger(classOf[ShopifyUserServiceLocatorActor])

    private val cache = WeakHashMap[String, ShopifyUserService]()
    private var cacheByPartnerId: ConcurrentMap[String, ShopifyUserService] = null
    private var cacheByShopDomain: ConcurrentMap[String, ShopifyUserService] = null

    @BeanProperty var shopifyUserServiceCreator: ShopifyUserServiceCreator = _
    @BeanProperty var cacheManager: CacheManager = _

    override def preStart() {
        cacheByPartnerId = cacheManager.getCache[ShopifyUserService](
            "PartnerUserServices",
            Some(new CacheListenerActorClient(self)))
    }

    def receive = {

        case msg @ LocateByPartnerId(partnerId: String ) =>
            val channel: Channel[LocateByPartnerIdResponse] = self.channel

            def error(e: Throwable) {
                channel ! LocateByPartnerIdResponse(msg, Left(ShopifyException("Error locating Partner Id")))
            }

            cacheByPartnerId.get(partnerId) match {
                case Some(shopifyUserService) =>
                    channel ! LocateByPartnerIdResponse(msg, Right(shopifyUserService))
                    logger.debug("Shopify Partner Id Cache Hit for Partner Id Key {}", partnerId)
                case _ =>
                    shopifyUserServiceCreator.createFromPartnerId(partnerId).onComplete(_.value.get.fold(
                        e => error(e),
                        _ match {
                            case CreateFromPartnerIdResponse(_ , Left(e)) => error(e)
                            case CreateFromPartnerIdResponse(_, Right(shopifyUserService)) =>
                                channel ! LocateByPartnerIdResponse(msg, Right(shopifyUserService))
                                cacheByPartnerId.put(partnerId, shopifyUserService)
                        }))
            }
        
        case msg @ LocateByToken(shop, signature, t, timeStamp) =>
            val channel: Channel[LocateByTokenResponse] = self.channel


            def error(e: Throwable) {
                channel ! LocateByTokenResponse(msg, Left(ShopifyException("Could not locate Shopify User", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating Shopfiy User Service From Token: {}" , t)
                shopifyUserServiceCreator.createFromToken(shop, signature, t, timeStamp).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case CreateFromTokenResponse(_, Left(e)) => error(e)
                        case CreateFromTokenResponse(_, Right(shopifyUserService)) =>
                            channel ! LocateByTokenResponse(msg, Right(shopifyUserService))
                }))
            } catch {
                case e =>
                    error(e)
            }

        case _ =>
            self.channel ! None
    }

}
