package com.echoed.chamber.services.shopify


import java.security.MessageDigest
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import akka.dispatch.Future
import akka.actor.{Channel, Actor}
import java.util.Properties
import com.echoed.chamber.dao.ShopifyUserDao
import com.echoed.chamber.domain.shopify.ShopifyUser

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 3/16/12
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */

class ShopifyUserServiceCreatorActor  extends Actor{
    
    private val logger = LoggerFactory.getLogger(classOf[ShopifyUserServiceCreatorActor])

    @BeanProperty var shopifyAccess: ShopifyAccess = _
    @BeanProperty var shopifyUserDao: ShopifyUserDao = _

    def updateShopifyUser(me: ShopifyUser) = {
        val shopifyUser = Option(shopifyUserDao.findByShopifyId(me.shopifyId)) match {
            case Some(su) =>
                logger.debug("Shopify Partner Found : {}", su)
                me.copy(partnerId = su.partnerId)
            case None =>
                me
        }
        shopifyUserDao.insertOrUpdate(shopifyUser)
        shopifyUser
    }

    def receive = {
        case msg @ CreateFromToken(shop, signature, t, timeStamp) =>

            val channel: Channel[CreateFromTokenResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateFromTokenResponse(msg, Left(ShopifyException("Could not create Shopify User Service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating Credentials for shop: {}", shop)
                shopifyAccess.fetchPassword(shop, signature, t, timeStamp).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case FetchPasswordResponse(_, Left(e)) => error(e)
                        case FetchPasswordResponse(_, Right(myPassword)) =>
                            shopifyAccess.fetchShop(shop, myPassword).onComplete(_.value.get.fold(
                                error(_),
                                _ match {
                                    case FetchShopResponse(_, Left(e)) => error(e)
                                    case FetchShopResponse(_, Right(me)) =>
                                        val shopifyUser = updateShopifyUser(me.copy(password = myPassword))
                                        channel ! CreateFromTokenResponse(msg, Right(new ShopifyUserServiceActorClient(Actor.actorOf(
                                        new ShopifyUserServiceActor(
                                            shopifyAccess,
                                            shopifyUserDao,
                                            shopifyUser)).start())))
                            }))
                    }))
            } catch {
                case e =>
                    logger.debug("exception {}" , e)
            }

        case msg @ CreateFromPartnerId(partnerId) =>
            val channel: Channel[CreateFromPartnerIdResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateFromPartnerIdResponse(msg, Left(ShopifyException("Could Not Create Shopify User Service", e)))
                logger.error("Error processing %s" format msg, e)
            }
            try {
                logger.debug("Creating Credentials for Shop From PartnerId: {}", partnerId)
                Option(shopifyUserDao.findByPartnerId(partnerId)) match {
                    case Some(shopifyUser) =>
                        shopifyAccess.fetchShop(shopifyUser.shopifyDomain, shopifyUser.password).onComplete(_.value.get.fold(
                            error(_),
                            _ match {
                                case FetchShopResponse(_, Left(e)) => error(e)
                                case FetchShopResponse(_, Right(shopifyUser)) =>
                                    channel ! CreateFromPartnerIdResponse(msg, Right(new ShopifyUserServiceActorClient(Actor.actorOf(
                                        new ShopifyUserServiceActor(
                                            shopifyAccess,
                                            shopifyUserDao,
                                            shopifyUser)).start())))
                            }))
                    case None =>
                }
            } catch {
                case e => error(e)
            }

        case msg @ CreateFromShopDomain(shopDomain) =>

            val channel: Channel[CreateFromShopDomainResponse] = self.channel

            try {
                //shopifyUserDao.findByShopDomain(shopDomain)
            } catch {
                case e =>
            }

    }

}
