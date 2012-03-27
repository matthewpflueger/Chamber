package com.echoed.chamber.services.shopify

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import akka.actor.{Channel, Actor}
import java.util.Properties
import java.util.HashMap

import com.shopify.api.credentials.Credential
import com.shopify.api.client.ShopifyClient
import com.shopify.api.APIAuthorization
import com.shopify.api.endpoints._
import com.shopify.api.resources._

import scala.collection.mutable.WeakHashMap

import com.echoed.chamber.domain.shopify.ShopifyUser
import collection.JavaConversions._

class ShopifyAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[ShopifyAccessActor])

    private val cache = WeakHashMap[String, ShopifyClient]()

    @BeanProperty var shopifySecret: String = _
    @BeanProperty var shopifyApiKey: String = _

    @BeanProperty var properties: Properties = _

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

        case msg @ FetchPassword(shop, signature, t, timeStamp ) =>
            val channel: Channel[FetchPasswordResponse] = self.channel
            channel ! FetchPasswordResponse(msg, Right(getShopifyPassword (shop, signature, t, timeStamp)))

        case msg @ FetchProduct(shop, password, productId) =>
            val channel: Channel[FetchProductResponse] = self.channel
            try {
                val shopifyClient = getShopifyClient(shop, password)
                val productService: ProductsService = shopifyClient.constructService(classOf[ProductsService])
                val product: Product = productService.getProduct(productId)
                channel ! FetchProductResponse(msg, Right(product))
            } catch {
                case e =>
                    channel ! FetchProductResponse(msg, Left(ShopifyException("Error retrieving products", e)))
            }

        case msg @ FetchProducts(shop, password) =>
            val channel: Channel[FetchProductsResponse] = self.channel
            try {
                val shopifyClient = getShopifyClient(shop, password)
                val productService: ProductsService = shopifyClient.constructService(classOf[ProductsService])
                //logger.debug("Found Products: {} ,", productService.getProducts.toString);
                val products = productService.getProducts.toList
                channel ! FetchProductsResponse(msg, Right(products))
            } catch{
                case e =>
                    logger.error("Error Fetching Products: {}", e)
                    channel ! FetchProductsResponse(msg, Left(ShopifyException("Error Fetching Products", e)))
            } 

        case msg @ FetchOrder(shop, password, orderId) =>
            val channel: Channel[FetchOrderResponse] = self.channel
            try {
                logger.debug("Fetching Order: {}", orderId)
                val shopifyClient = getShopifyClient(shop,password)
                val orderService: OrdersService = shopifyClient.constructService(classOf[OrdersService])
                val order: Order = orderService.getOrder(orderId)
                logger.debug("Received Order: {} ", order)
                channel ! FetchOrderResponse(msg, Right(order))
            } catch {
                case e =>
                    logger.error("Error fetching order: {}", e)
                    channel ! FetchOrderResponse(msg, Left(ShopifyException("Error retrieving orders", e)))
            }
            
        case msg @ FetchShop(shop, password) =>
            val channel: Channel[FetchShopResponse] = self.channel
            try {
                val shopifyClient = getShopifyClient(shop, password)
                val shopService: ShopService = shopifyClient.constructService(classOf[ShopService])
                val shopObject: Shop = shopService.getShop
                val shopifyUser = new ShopifyUser(shopObject)
                logger.debug("Received Shop: {} ", shopObject)
                channel ! FetchShopResponse(msg, Right(shopifyUser))
            }
            catch {
                case e =>
                    logger.error("{}",e)
                    channel ! FetchShopResponse(msg, Left(ShopifyException("Error retrieving credentials",e)))
            }
        case _ =>
    }
    
    private def getShopifyPassword(shop: String, signature: String, t: String, timeStamp: String): String = {
        try {

            val hashMap = new HashMap[String, String]()
            hashMap.put("shop", shop)
            hashMap.put("t", t)
            hashMap.put("timestamp", timeStamp)
            hashMap.put("signature", signature)
            val credential = new Credential(shopifyApiKey,shopifySecret,shop)
            val auth: APIAuthorization = new APIAuthorization(credential)
            var password: String = null
            if(auth.computeAPIPassword(hashMap)){
                password = credential.getPassword()
            }
            password
        } catch {
            case e =>
                null
        }
    }

    private def getShopifyClient(shop: String, password: String): ShopifyClient = {
        cache.getOrElse(shop, {
            try {
                val credential = new Credential(shopifyApiKey ,shopifySecret , shop, password)
                val shopifyClient = new ShopifyClient(credential)
                cache(shop) = shopifyClient
                shopifyClient
            } catch {
                case e =>
                    null
            }    
        })
    }
    
}
