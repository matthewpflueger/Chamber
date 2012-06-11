package com.echoed.chamber.services.partner.magentogo

import reflect.BeanProperty
import org.slf4j.LoggerFactory

import dispatch._
import dispatch.nio.Http

import scalaz._
import Scalaz._


import com.echoed.chamber.services.partner.{EchoItem, EchoRequest}
import xml.NodeSeq


import com.echoed.chamber.domain.partner.magentogo.MagentoGoCredentials
import org.joda.time.format.DateTimeFormat
import collection.mutable.{ConcurrentMap, ListBuffer => MList, HashMap => MMap}
import xml.Node

import akka.actor._
import com.echoed.cache.CacheManager
import akka.util.Timeout
import akka.util.duration._
import akka.event.Logging
import akka.pattern.ask
import org.springframework.beans.factory.FactoryBean


class MagentoGoAccessActor extends FactoryBean[ActorRef] {

    //example: 2012-05-21 02:05:05
    private final val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

    @BeanProperty var client: Http = _
    @BeanProperty var cacheManager: CacheManager = _


    val urn = "urn:Magento"
    private val contentType = Map("Accept" -> "application/soap+xml")
    private val encoding = "utf-8"

    private var cache: ConcurrentMap[String, String] = _


    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    override def preStart {
        require(client != null, "Required Http client is missing")
        cache = cacheManager.getCache[String]("MagentoGoSessions")
    }


    def value(node: Node) = {
        val valueType = node.attributes.value.toString

        if (valueType.endsWith("Map")) asMap(node)
        else if (valueType.endsWith("Array")) asList(node)
        else node.text
    }

    def asList(node: Node): List[Any] = {
        val list = MList[Any]()
        node.child.foreach { i =>
            list.append(value(i))
        }
        list.toList
    }

    def asMap(node: Node): Map[String, Any] = {
        val map = MMap[String, Any]()
        node.child.foreach { i =>
            val key = (i \ "key").head.text
            map(key) = value((i \ "value").head)
        }
        map.toMap
    }

    def wrap(c: MagentoGoCredentials, body: NodeSeq): Request = {
        val content =
            <env:Envelope
                    xmlns="http://schemas.xmlsoap.org/wsdl/"
                    xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                    xmlns:enc="http://schemas.xmlsoap.org/soap/encoding/"
                    xmlns:env="http://www.w3.org/2003/05/soap-envelope"
                    xmlns:ns1={urn}
                    xmlns:ns2="http://xml.apache.org/xml-soap">
                <env:Body>
                    {body}
                </env:Body>
            </env:Envelope>

        url(c.apiPath)
            .<:<(contentType)
            .>\(encoding)
            .<<(content.toString)
    }


    def products(c: MagentoGoCredentials, session: String, products: List[String]) = {
        val nodes = products.map { p =>
            <item xsi:type="ns2:Array" enc:arraySize="2"><item>catalog_product.info</item><item>{p}</item></item>
            <item xsi:type="ns2:Array" enc:arraySize="2"><item>product_media.list</item><item>{p}</item></item>
        }
        wrap(c,
            <multiCall xmlns={urn}>
                <sessionId>{session}</sessionId>
                <calls>{nodes}</calls>
            </multiCall>)
    }

    private def order(c: MagentoGoCredentials, session: String, order: Long) =
        wrap(c,
            <call>
                <sessionId>{session}</sessionId>
                <resourcePath>sales_order.info</resourcePath>
                <args>{order}</args>
            </call>)


    private def login(c: MagentoGoCredentials) =
        wrap(c,
            <login xmlns={urn}>
                <username>{c.apiUser}</username>
                <apiKey>{c.apiKey}</apiKey>
            </login>)


    def receive = {
        case msg @ Validate(credentials) =>
            val channel = context.sender

            client(login(credentials) <> { res =>
                logger.debug("Login response for {}: {}", credentials, res)
                (res \\ "loginReturn").headOption.cata(
                    node => {
                        logger.debug("Received session {} identifier for {}", node.text, credentials)
                        cache(credentials.apiPath) = node.text
                        channel ! ValidateResponse(msg, Right(node.text))
                    },
                    {
                        logger.warning("Received error response validating {}: {}", credentials, res)
                        channel ! ValidateResponse(msg, Left(MagentoGoPartnerException("Could not validate credentials")))
                    })
            } >! {
                case e =>
                    logger.error("Error validating %s" format credentials, e)
                    channel ! ValidateResponse(msg, Left(MagentoGoPartnerException("Error during login of %s" format credentials, e)))
            })


        case msg @ FetchOrder(credentials, orderId) =>
            val me = context.self
            val channel = context.sender

            def error(e: Throwable) {
                logger.error("Error fetching order %s for %s" format(orderId, credentials), e)
                e match {
                    case mgpe: MagentoGoPartnerException => channel ! FetchOrderResponse(msg, Left(mgpe))
                    case _ => channel ! FetchOrderResponse(
                            msg,
                            Left(MagentoGoPartnerException("Error fetching order %s using %s" format(orderId, credentials))))
                }
            }

            def fetchOrder(sessionId: String) {
                logger.debug("Fetching order {} for {}", orderId, credentials)
                client(order(credentials, sessionId, orderId) <> { res =>
                    logger.debug("Received order {} for {}", orderId, credentials)
                    val orderResult = asMap((res \\ "callReturn").head)
                    val productIds = orderResult("items").asInstanceOf[List[Map[String, String]]].map(_("product_id"))

                    logger.debug("Fetching products {} for {}", productIds, credentials)
                    client(products(credentials, sessionId, productIds) <> { res =>
                        logger.debug("Received products {} for {}", productIds, credentials)
                        var index = 0
                        val echoItems = (asList((res \\ "multiCallReturn").head).partition { _ =>
                            val even = index % 2 == 0
                            index += 1
                            even
                        }.zipped.map { (x, y) => (x, y) }).map { tuple =>
                            val product = tuple._1.asInstanceOf[Map[String, Any]]
                            val images = tuple._2.asInstanceOf[List[Map[String, Any]]]

                            new EchoItem(
                                product.get("product_id").map(_.toString).orNull,
                                product.get("name").map(_.toString).orNull,
                                null,
                                null,
                                product.get("price").map(p => java.lang.Float.parseFloat(p.toString)).getOrElse(0f),
                                images.headOption.map(_("url")).getOrElse("").toString,
                                product.get("url_path").map(_.toString).orNull,
                                product.get("short_description").map(_.toString).orNull)
                        }.filter { i =>
                            if (!i.isValid) logger.debug("Filtered invalid EchoItem {}", i)
                            i.isValid
                        }.toList

                        val echoRequest = new EchoRequest(
                            orderId.toString,
                            orderResult("customer_id").toString,
                            dateFormatter.parseDateTime(orderResult("created_at").toString).toDate,
                            echoItems)
                        channel ! FetchOrderResponse(msg, Right(echoRequest))
                        logger.debug("Finished fetching order {} for {}", orderId, credentials)
                    } >! {
                        case e => error(e)
                    })
                } >! {
                    case e => error(e)
                })
            }

            cache.get(credentials.apiPath).cata(
                fetchOrder(_),
                {
                    logger.debug("Creating session for {}", credentials)
                    (me ? Validate(credentials)).mapTo[ValidateResponse].onComplete(_.fold(
                        e => error(e),
                        _ match {
                            case ValidateResponse(_, Left(e)) => error(e)
                            case ValidateResponse(_, Right(sessionId)) =>
                                logger.debug("Successfully created session for {}", credentials)
                                fetchOrder(sessionId)
                        }))
                })
    }

    }), "MagentoGoAccess")
}
