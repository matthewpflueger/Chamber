package com.echoed.chamber.services.partner.networksolutions

import reflect.BeanProperty
import org.slf4j.LoggerFactory

import scalaxb.{DataRecord, SoapClients, DispatchHttpClients}
import akka.actor.{Channel, Actor}


import scalaz._
import Scalaz._
import java.util.{Date, Properties}
import com.echoed.chamber.services.partner.{EchoItem, EchoRequest}
import com.echoed.networksolutions.ecomapi._


class NetworkSolutionsAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[NetworkSolutionsAccessActor])

    @BeanProperty var application: String = _
    @BeanProperty var certificate: String = _

    @BeanProperty var properties: Properties = _

    //these are here so we don't have to type Some(application or certificate) everywhere...
    private var appId: Option[String] = _
    private var cert: Option[String] = _
    private val attr = Map[String, DataRecord[Any]]()
    private val detailSize = Some(Large)


    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        //where placeholder values were not being resolved
        {
            application = properties.getProperty("application")
            certificate = properties.getProperty("certificate")

            application != null && certificate != null
        } ensuring(_ == true, "Missing parameters")

        appId = Some(application)
        cert = Some(certificate)
    }

    //if IntelliJ tells you that object creation is impossible - ignore it!
    val client = (new NetSolEcomServiceSoap12Bindings with SoapClients with DispatchHttpClients).service


    private def logErrors(errorList: Seq[ErrorType]) {
        errorList.foreach { e => logger.error("%s" format e) }
    }

    def receive = {

        case msg @ FetchUserKey(successUrl, failureUrl) =>
            val channel: Channel[FetchUserKeyResponse] = self.channel

            client.getUserKey(
                    new GetUserKeyRequestType(UserKey = Some(new UserKeyType(SuccessUrl = Some(successUrl), FailureUrl = failureUrl))),
                    new SecurityCredentialType(Application = appId, Certificate = cert, attributes = attr)).fold(
                fault => {
                    logger.error("Received error fetching user key %s" format fault)
                    channel ! FetchUserKeyResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching user key %s" format fault)))
                },
                response => {
                    logErrors(response.ErrorList)
                    response.UserKey.cata(
                        k => channel ! FetchUserKeyResponse(msg, Right(FetchUserKeyEnvelope(k.LoginUrl.get, k.UserKey.get))),
                        channel ! FetchUserKeyResponse(msg, Left(NetworkSolutionsPartnerException("Error fetching user key")))
                    )})


        case msg @ FetchUserToken(userKey) =>
            val channel: Channel[FetchUserTokenResponse] = self.channel

            client.getUserToken(
                    new GetUserTokenRequestType(UserToken = Some(UserTokenType(UserKey = Some(userKey)))),
                    new SecurityCredentialType(Application = appId, Certificate = cert, attributes = attr)).fold(
                fault => {
                    logger.error("Received error fetching user token %s" format fault)
                    channel ! FetchUserTokenResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching user token %s" format fault)))
                },
                response => {
                    logErrors(response.ErrorList)
                    response.UserToken.cata(
                        t => {
                            val userToken = t.UserToken.get
                            val expiresOn = t.Expiration.get.toGregorianCalendar.getTime

                            client.readSiteSetting(
                                    new ReadSiteSettingRequestType(DetailSize = detailSize),
                                    new SecurityCredentialType(appId, cert, Some(userToken), attr)).fold(
                                fault => {
                                    logger.error("Received error fetching site settings %s" format fault)
                                    channel ! FetchUserTokenResponse(
                                            msg,
                                            Left(NetworkSolutionsPartnerException("Error fetching site settings %s" format fault)))
                                },
                                s => {
                                    logErrors(s.ErrorList)
                                    val settings = s.SiteSetting.get
                                    channel ! FetchUserTokenResponse(
                                            msg,
                                            Right(FetchUserTokenEnvelope(
                                                userToken,
                                                expiresOn,
                                                settings.CompanyName.get,
                                                settings.StoreUrl.get,
                                                settings.StoreSecureUrl.get)))
                                })
                        },
                        channel ! FetchUserTokenResponse(
                                msg,
                                Left(NetworkSolutionsPartnerException("Error fetching user token - no token in response")))
                    )})


        case msg @ FetchOrder(userToken, orderNumber) =>
            //override the Scalaz imports in this scope...
            import com.echoed.networksolutions.ecomapi.Equal
            import com.echoed.networksolutions.ecomapi.In

            val channel: Channel[FetchOrderResponse] = self.channel

            client.readOrder(
                    new ReadOrderRequestType(
                        DetailSize = Some(Large),
                        FilterList = List[FilterType](new FilterType(
                            Field = Some("OrderNumber"),
                            Operator = Some(Equal),
                            ValueList = List[String](orderNumber.toString)))),
                    new SecurityCredentialType(appId, cert, Some(userToken), attr)).fold(
                fault => {
                    logger.error("Received error fetching order %s with user token %s: %s".format(orderNumber, userToken, fault))
                    channel ! FetchOrderResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching order %s" format orderNumber)))
                },
                response => {
                    logErrors(response.ErrorList)
                    val order = response.OrderList.head
                    val productIds = order.Invoice.get.LineItemList.map(_.ProductId.get.toString)

                    client.readProduct(
                            new ReadProductRequestType(
                                DetailSize = Some(Large),
                                FilterList = List[FilterType](new FilterType(
                                    Field = Some("ProductId"),
                                    Operator = Some(In),
                                    ValueList = productIds))),
                            new SecurityCredentialType(appId, cert, Some(userToken), attr)).fold(
                        fault => {
                            logger.error("Received error fetching products for order %s with user token %s: %s".format(orderNumber, userToken, fault))
                            channel ! FetchOrderResponse(
                                    msg,
                                    Left(NetworkSolutionsPartnerException("Error fetching products for order %s" format orderNumber)))
                        },
                        products => {
                            logErrors(products.ErrorList)
                            channel ! FetchOrderResponse(
                                    msg,
                                    Right(EchoRequest(
                                            orderNumber.toString,
                                            order.Customer.map(_.CustomerNumber.getOrElse("")).get,
                                            order.CreateDate.map(_.toGregorianCalendar.getTime).getOrElse(new Date()),
                                            products.ProductList.map { p => EchoItem(
                                                productId = p.ProductId.get.toString,
                                                productName = p.Name.orNull,
                                                category = p.CategoryList.filter(_.Name.isDefined).headOption.map(_.Name.get).orNull,
                                                brand = null,
                                                price = p.Price.map(_.BasePrice.get.value.toFloat).get,
                                                imageUrl = p.ImageList.headOption.map { i =>
                                                                i.DisplayUrl.orElse(
                                                                i.DetailUrl.orElse(
                                                                i.ThumbnailUrl)).get
                                                }.get,
                                                landingPageUrl = p.PageUrl.get,
                                                description = p.Description.orNull)
                                            }.toList)))
                        })
                })

    }

}
