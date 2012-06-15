package com.echoed.chamber.services.partner.bigcommerce

import reflect.BeanProperty
import java.util.{Map => JMap}
import collection.JavaConversions._
import collection.mutable.{ListBuffer => MList, HashMap => MMap}

import dispatch._

import com.echoed.chamber.services.partner.{EchoItem, EchoRequest}
import com.echoed.util.ScalaJson._
import com.echoed.chamber.domain.partner.bigcommerce.BigCommerceCredentials
import org.joda.time.format.DateTimeFormat
import akka.actor._
import org.springframework.beans.factory.FactoryBean
import akka.util.Timeout
import akka.event.Logging
import akka.util.duration._
import akka.actor.SupervisorStrategy.Stop


class BigCommerceAccessActor extends FactoryBean[ActorRef] {

    //example: Mon, 14 May 2012 18:11:26 +0000
    private final val dateFormatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")

    @BeanProperty var client: Http = _

    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Stop
    }

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    override def preStart {
        require(client != null, "Required Http client is missing")
    }


    private def endpoint(c: BigCommerceCredentials, path: String) =
        url(c.apiPath)
            .as_!(c.apiUser, c.apiToken)
            ./(path)
            .addHeader("Accept", "application/json; charset=utf-8")


    private def request(c: BigCommerceCredentials, path: String)
            (callback: String => Unit)
            (error: PartialFunction[Throwable, Unit]) =
        client(endpoint(c, path) OK As.string).onSuccess {
            case s => callback(s)
        }.onFailure {
            case e => error(e)
        }


    private def requestAsMap(c: BigCommerceCredentials, path: String)
            (callback: Map[String, AnyRef] => Unit)
            (error: PartialFunction[Throwable, Unit]) =
        request(c, path) { in => callback(parse[Map[String, AnyRef]](in).withDefaultValue("")) } (error)

    private def requestAsArray(c: BigCommerceCredentials, path: String)
            (callback: Array[JMap[String, AnyRef]] => Unit)
            (error: PartialFunction[Throwable, Unit]) =
        request(c, path) { in => callback(parse[Array[JMap[String, AnyRef]]](in)) } (error)


    def receive = {
        /*case msg @ ('error, e: IllegalStateException) =>
            logger.error("Restarting Http client due to error", e)
            client.shutdown()
            client = new Http*/

        case msg @ ('error, e: Throwable) =>
            logger.error("Received error but not restarting Http client: {}", e)

        case msg @ Validate(credentials) =>
            val channel = context.sender
            requestAsMap(credentials, "time") { map =>
                channel ! ValidateResponse(msg, Right(map.contains("time")))
            } {
                case e =>
                    logger.error("Error validating {}", credentials)
                    channel ! ValidateResponse(
                        msg,
                        Left(BigCommercePartnerException("Error validating BigCommerce api connection", e)))
            }

        case msg @ FetchOrder(credentials, order) =>
            val me = context.self
            val channel = context.sender

            val orderActor = context.actorOf(Props(new Actor {
                var expected = -1
                var products = 0
                var images = 0
                var responseSent = false

                var echoRequest: EchoRequest = null
                val echoItems = MList[EchoItem]()
                val imageMap = MMap[String, String]()

                override def preStart {
                    //our lifespan is one minute...
                    context.system.scheduler.scheduleOnce(1 minutes, context.self, 'timetodie)
                }

                def complete {
                    logger.debug(
                        "Checking if order {} fetch is complete: expected {}, products {}, images {}, and echoRequest {}",
                        Array(order, expected, products, images, echoRequest))

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
                            logger.error("Fetch of BigCommerce order {} timed out with {}", order, credentials)
                            channel ! FetchOrderResponse(
                                msg,
                                Left(BigCommercePartnerException("Error fetching BigCommerce order %s" format order)))
                        }

                    case ('errorProduct, e: Throwable) =>
                        logger.error("Error fetching product for order {} with {}: {}", order, credentials, e)
                        products += 1
                        complete
                        me ! ('error, e)
                    case ('errorImage, productId: String, e: Throwable) =>
                        logger.error("Error fetching image for product {} for order {} with {}: {}", productId, order, credentials, e)
                        images += 1
                        complete
                        me ! ('error, e)
                    case ('errorOrder, orderId: String, e: Throwable) =>
                        logger.error("Error fetching order {} with {}: {}", orderId, credentials, e)
                        channel ! FetchOrderResponse(
                            msg,
                            Left(BigCommercePartnerException("Error fetching BigCommerce order %s" format order, e)))
                        responseSent = true
                        me ! ('error, e)
                }
            }))


            logger.debug("Fetching BigCommerce order {} with {}", order, credentials)
            requestAsMap(credentials, "orders/%s" format order) { map =>
                logger.debug("Received info for order {} with {}", order, credentials)
                orderActor ! EchoRequest(
                        order.toString,
                        map("customer_id").toString,
                        dateFormatter.parseDateTime(map("date_created").toString).toDate,
                        null)
            } { case e => orderActor ! ('errorOrder, "Error fetching order %s with %s" format(order, credentials), e) }

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

    }), "BigCommerceAccess")
}
