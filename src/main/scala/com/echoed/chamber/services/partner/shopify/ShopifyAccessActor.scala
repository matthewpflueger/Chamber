package com.echoed.chamber.services.partner.shopify

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import java.util.Properties
import java.util.HashMap

import com.shopify.api.credentials.Credential
import com.shopify.api.client.ShopifyClient
import com.shopify.api.APIAuthorization
import com.shopify.api.endpoints._
import com.shopify.api.resources._


import com.echoed.chamber.domain.partner.shopify.ShopifyPartner
import collection.JavaConversions._
import java.util.concurrent.ConcurrentHashMap
import collection.mutable.ConcurrentMap
import org.springframework.beans.factory.FactoryBean
import akka.actor._
import akka.util.Timeout
import akka.util.duration._
import akka.event.Logging

class ShopifyAccessActor extends FactoryBean[ActorRef] {


    private val cache: ConcurrentMap[String, ShopifyClient] = new ConcurrentHashMap[String, ShopifyClient]()

    @BeanProperty var shopifySecret: String = _
    @BeanProperty var shopifyApiKey: String = _

    @BeanProperty var properties: Properties = _


    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        //where placeholder values were not being resolved
        {
            shopifySecret = properties.getProperty("shopifySecret")
            shopifyApiKey = properties.getProperty("shopifyApiKey")

            shopifySecret != null && shopifyApiKey != null //&& callbackUrl != null
        } ensuring(_ == true, "Missing parameters")
    }

    def receive = {

        case msg @ FetchPassword(shop, signature, t, timeStamp) =>
            val channel = context.sender
            channel ! FetchPasswordResponse(msg, Right(getShopifyPassword(shop, signature, t, timeStamp)))

        case msg @ FetchProduct(shop, password, productId) =>
            val channel = context.sender
            try {
                val shopifyClient = getShopifyClient(shop, password)
                val productService: ProductsService = shopifyClient.constructService(classOf[ProductsService])
                val product: Product = productService.getProduct(productId)
                channel ! FetchProductResponse(msg, Right(product))
            } catch {
                case e =>
                    channel ! FetchProductResponse(msg, Left(ShopifyPartnerException("Error retrieving product %s for shop %s" format(productId, shop), e)))
                    logger.error("Error fetching product {} for shop {}: {}", productId, shop, e)
            }

        case msg @ FetchProducts(shop, password) =>
            val channel = context.sender
            try {
                val shopifyClient = getShopifyClient(shop, password)
                val productService: ProductsService = shopifyClient.constructService(classOf[ProductsService])
                val products = productService.getProducts.toList
                channel ! FetchProductsResponse(msg, Right(products))
            } catch {
                case e =>
                    channel ! FetchProductsResponse(msg, Left(ShopifyPartnerException("Error fetching products for shop %s" format(shop), e)))
                    logger.error("Error fetching products for shop {}: {}", shop, e)
            }

        case msg @ FetchOrder(shop, password, orderId) =>
            val channel = context.sender
            try {
                logger.debug("Fetching order {}", orderId)
                val shopifyClient = getShopifyClient(shop, password)
                val orderService: OrdersService = shopifyClient.constructService(classOf[OrdersService])
                val order: Order = orderService.getOrder(orderId)
                logger.debug("Received order {}", order)
                channel ! FetchOrderResponse(msg, Right(order))
            } catch {
                case e =>
                    channel ! FetchOrderResponse(msg, Left(ShopifyPartnerException("Error retrieving order %s for shop %s" format(orderId, shop), e)))
                    logger.error("Error fetching order {} for shop {}: {}", orderId, shop, e)
            }

        case msg @ FetchShopFromToken(shop, signature, t, timeStamp) =>
            val channel = context.sender

            try {
                val password = getShopifyPassword(shop, signature, t, timeStamp)
                val shopifyClient = getShopifyClient(shop, password)
                val shopService: ShopService = shopifyClient.constructService(classOf[ShopService])
                val shopObject: Shop = shopService.getShop
                val shopifyPartner = new ShopifyPartner(shopObject, password)
                logger.debug("Received Shop: {} ", shopObject)
                channel ! FetchShopFromTokenResponse(msg, Right(shopifyPartner))
            } catch {
                case e =>
                    channel ! FetchShopFromTokenResponse(msg, Left(ShopifyPartnerException("Error fetching shop %s" format shop, e)))
                    logger.error("Error fetching shop {}: {}", shop, e)
            }


        case msg @ FetchShop(shop, password) =>
            val channel = context.sender
            try {
                logger.debug("Fetching shop {}", shop)
                val shopifyClient = getShopifyClient(shop, password)
                val shopService: ShopService = shopifyClient.constructService(classOf[ShopService])
                val shopObject: Shop = shopService.getShop
                val shopifyUser = new ShopifyPartner(shopObject, password)
                logger.debug("Received shop {}", shopObject)
                channel ! FetchShopResponse(msg, Right(shopifyUser))
            }
            catch {
                case e =>
                    channel ! FetchShopResponse(msg, Left(ShopifyPartnerException("Error retrieving shop %s" format shop, e)))
                    logger.error("Error retrieving shop {}: {}", shop, e)
            }
    }


    private def getShopifyPassword(shop: String, signature: String, t: String, timeStamp: String): String = {
        val hashMap = new HashMap[String, String]()
        hashMap.put("shop", shop)
        hashMap.put("t", t)
        hashMap.put("timestamp", timeStamp)
        hashMap.put("signature", signature)
        val credential = new Credential(shopifyApiKey, shopifySecret, shop)
        val auth: APIAuthorization = new APIAuthorization(credential)
        var password: String = null
        if (auth.computeAPIPassword(hashMap)) {
            password = credential.getPassword()
        }
        password
    }


    private def getShopifyClient(shop: String, password: String): ShopifyClient = {
        val client = cache.getOrElse(shop, {
            val credential = new Credential(shopifyApiKey, shopifySecret, shop, password)
            val shopifyClient = new ShopifyClient(credential)
            cache(shop) = shopifyClient
            shopifyClient
        })
        if (client.getCredential.getPassword != password) {
            logger.debug("Shopify client {} has reset their password - constructing new client", client.getCredential.getShopName)
            cache.remove(shop)
            getShopifyClient(shop, password)
        } else client
    }

    }), "ShopifyAccess")
}
