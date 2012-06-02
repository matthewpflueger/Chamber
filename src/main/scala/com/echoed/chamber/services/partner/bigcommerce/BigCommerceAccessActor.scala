package com.echoed.chamber.services.partner.bigcommerce

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import java.util.{Map => JMap}
import collection.JavaConversions._
import collection.mutable.{ListBuffer => MList, HashMap => MMap}

import dispatch._
import dispatch.nio.Http


import java.util.Properties
import com.echoed.chamber.services.partner.{EchoItem, EchoRequest}
import xml.NodeSeq
import java.io.InputStream
import com.echoed.util.ScalaJson._
import akka.actor.{Scheduler, PoisonPill, Channel, Actor}
import java.util.concurrent.TimeUnit
import akka.dispatch.{DefaultCompletableFuture, Future}
import com.echoed.chamber.domain.partner.bigcommerce.BigCommerceCredentials
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}


class BigCommerceAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[BigCommerceAccessActor])

    //example: Mon, 14 May 2012 18:11:26 +0000
    private final val dateFormatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")

    @BeanProperty var client: Http = _

    private val contentType = Map("Accept" -> "application/json")
    private val encoding = "utf-8"


    override def preStart {
        require(client != null, "Required Http client is missing")
    }



    private def endpoint(c: BigCommerceCredentials, path: String) =
        url(c.apiPath)
            .as_!(c.apiUser, c.apiToken)
            ./(path)
            .<:<(contentType)
            .>\(encoding)

    private def request(c: BigCommerceCredentials, path: String)
            (callback: InputStream => Unit)
            (error: ExceptionListener) = client(endpoint(c, path).>>(callback).>!(error))


    private def requestAsMap(c: BigCommerceCredentials, path: String)
            (callback: Map[String, AnyRef] => Unit)
            (error: ExceptionListener) =
        request(c, path) { in => callback(parse[Map[String, AnyRef]](in).withDefaultValue("")) } (error)

    private def requestAsArray(c: BigCommerceCredentials, path: String)
            (callback: Array[JMap[String, AnyRef]] => Unit)
            (error: ExceptionListener) =
        request(c, path) { in => callback(parse[Array[JMap[String, AnyRef]]](in)) } (error)


    def receive = {
        case msg @ ('error, e: IllegalStateException) =>
            logger.error("Restarting Http client due to error", e)
            client.shutdown()
            client = new Http
        case msg @ ('error, e: Throwable) =>
            logger.error("Received error but not restarting Http client", e)

        case msg @ Validate(credentials) =>
            val channel: Channel[ValidateResponse] = self.channel
            requestAsMap(credentials, "time") { map =>
                channel ! ValidateResponse(msg, Right(map.contains("time")))
            } {
                case e =>
                    logger.error("Error validating %s" format credentials)
                    channel ! ValidateResponse(
                        msg,
                        Left(BigCommercePartnerException("Error validating BigCommerce api connection", e)))
            }

        case msg @ FetchOrder(credentials, order) =>
            val me = self
            val channel: Channel[FetchOrderResponse] = self.channel

            val orderActor = Actor.actorOf(new Actor {
                var expected = -1
                var products = 0
                var images = 0
                var responseSent = false

                var echoRequest: EchoRequest = null
                val echoItems = MList[EchoItem]()
                val imageMap = MMap[String, String]()

                override def preStart {
                    //our lifespan is one minute...
                    Scheduler.scheduleOnce(self, 'timetodie, 1, TimeUnit.MINUTES)
                }

                def complete {
                    logger.debug("Checking if order %s fetch is complete: expected %s, products %s, images %s, and echoRequest %s" format(
                        order, expected, products, images, echoRequest))
                    if (!responseSent && expected == products && expected == images && echoRequest != null) {
                        logger.debug("Completed fetching order {} for {}", order, credentials)
                        channel ! FetchOrderResponse(msg, Right(echoRequest.copy(items = echoItems.map { i =>
                                i.copy(imageUrl = imageMap.get(i.productId).orNull)
                            }.filter(_.isValid).toList)))
                        responseSent = true
                    } else if (responseSent) {
                        logger.debug("Response for order {} already sent", order)
                    } else {
                        logger.debug("Order {} not complete - still waiting for {} responses", order, (expected * 2 - products - images))
                    }
                }

                def receive = {
                    case ('forCompletion, num: Int) =>
                        logger.debug("Order actor for {} received 'forCompletion {}", order, num)
                        expected = num
                        complete
                    case er: EchoRequest =>
                        logger.debug("Order actor for {} received {}", order, er)
                        echoRequest = er
                        complete
                    case echoItem: EchoItem =>
                        logger.debug("Order actor for {} received {}", order, echoItem)
                        products += 1
                        echoItems += echoItem
                        complete
                    case ('image, productId: String, imageUrl: String) =>
                        logger.debug("Order actor for {} received {}", order, imageUrl)
                        images += 1
                        imageMap(productId) = imageUrl
                        complete

                    case 'timetodie =>
                        self ! PoisonPill
                        if (!responseSent) {
                            logger.error("Fetch of BigCommerce order %s timed out with %s" format(order, credentials))
                            channel ! FetchOrderResponse(
                                msg,
                                Left(BigCommercePartnerException("Error fetching BigCommerce order %s" format order)))
                        }

                    case ('errorProduct, e: Throwable) =>
                        logger.error("Error fetching product for order %s with %s" format(order, credentials), e)
                        products += 1
                        complete
                        me ! ('error, e)
                    case ('errorImage, productId: String, e: Throwable) =>
                        logger.error("Error fetching image for product %s for order %s with %s" format(productId, order, credentials), e)
                        images += 1
                        complete
                        me ! ('error, e)
                    case ('error, message: String, e: Throwable) =>
                        logger.error(message, e)
                        channel ! FetchOrderResponse(
                            msg,
                            Left(BigCommercePartnerException("Error fetching BigCommerce order %s" format order, e)))
                        responseSent = true
                        me ! ('error, e)
                }
            }).start


            logger.debug("Fetching BigCommerce order {} with {}", order, credentials)
            requestAsMap(credentials, "orders/%s" format order) { map =>
                logger.debug("Received info for order {} with {}", order, credentials)
                orderActor ! EchoRequest(
                        order.toString,
                        map("customer_id").toString,
                        dateFormatter.parseDateTime(map("date_created").toString).toDate,
                        null)
            } { case e => orderActor ! ('error, "Error fetching order %s with %s" format(order, credentials), e) }

            logger.debug("Fetching BigCommerce products for order {} with {}", order, credentials)
            requestAsArray(credentials, "orders/%s/products" format order) { products =>
                logger.debug("Received list of {} products for order {}", products.length, order)
                orderActor ! ('forCompletion, products.length)

                products.foreach { product =>
                    val productId = product("product_id").toString
                    logger.debug("Fetching info for product {} with {}", productId, credentials)

                    requestAsMap(credentials, "products/%s" format productId) { p =>
                        logger.debug("Received info for product {} with {}", productId, credentials)
                        orderActor ! EchoItem(
                                p("id").toString,
                                p("name").toString,
                                p("brand_id").toString,
                                p("categories").toString,
                                java.lang.Float.parseFloat(p("price").toString),
                                null,
                                //for some reason custom_url actually gets set as null?!?!
                                Option(p("custom_url")).map(_.toString).orNull,
                                p("description").toString)
                    } { case e => orderActor ! ('errorProduct, e) }

                    requestAsArray(credentials, "products/%s/images" format productId) { images =>
                        logger.debug("Received image info for product {} with {}", productId, credentials)
                        images.filter(_("is_thumbnail").toString == "true").headOption.orElse {
                            throw BigCommercePartnerException("No image specified for product %s for order %s" format(productId, order))
                        }.foreach { image =>
                            orderActor ! ('image, image("product_id").toString, "/%s" format image("image_file"))
                        }
                    } { case e => orderActor ! ('errorImage, productId, e) }
                }
            } { case e => orderActor ! ('error, "Error fetching products for order %s with %s" format(order, credentials), e) }
    }

}
