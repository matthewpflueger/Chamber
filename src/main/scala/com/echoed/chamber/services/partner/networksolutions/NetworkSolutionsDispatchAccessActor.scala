package com.echoed.chamber.services.partner.networksolutions

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.joda.time.format.ISODateTimeFormat

import akka.actor.{Channel, Actor}

import dispatch.nio.Http


import java.util.Properties
import com.echoed.chamber.services.partner.{EchoItem, EchoRequest}
import xml.NodeSeq
import dispatch.{Request, url}


class NetworkSolutionsDispatchAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[NetworkSolutionsDispatchAccessActor])

    private final val xmlDateTimeFormat = ISODateTimeFormat.dateTimeNoMillis

    @BeanProperty var application: String = _
    @BeanProperty var certificate: String = _
    @BeanProperty var client: Http = _

    val urn = "urn:networksolutions:apis"
    val endpoint = url("https://ecomapi.networksolutions.com/SoapService.asmx")

    @BeanProperty var properties: Properties = _

    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        //where placeholder values were not being resolved
        {
            application = properties.getProperty("application")
            certificate = properties.getProperty("certificate")

            application != null && certificate != null
        } ensuring(_ == true, "Missing parameters")

        require(client != null, "Missing required http client")
    }


    private def getUserKey(successUrl: String, failureUrl: Option[String] = None) =
            wrap(
                <GetUserKeyRequest xmlns={urn}>
                    <UserKey>
                        <SuccessUrl>{successUrl}</SuccessUrl>
                        <FailureUrl>{failureUrl.getOrElse("")}</FailureUrl>
                    </UserKey>
                </GetUserKeyRequest>)


    private def getUserToken(userKey: String) =
            wrap(
                <GetUserTokenRequest xmlns={urn}>
                    <UserToken>
                        <UserKey>{userKey}</UserKey>
                    </UserToken>
                </GetUserTokenRequest>)


    private def readSiteSettings(userToken: String) =
        wrap(
            <ReadSiteSettingRequest xmlns={urn}>
                <DetailSize>Large</DetailSize>
            </ReadSiteSettingRequest>,
            Some(userToken))


    private def readOrder(orderNumber: Long, userToken: String) =
        wrap(
            <ReadOrderRequest xmlns={urn}>
                <DetailSize>Large</DetailSize>
                <FilterList>
                    <Field>OrderNumber</Field>
                    <Operator>Equal</Operator>
                    <ValueList>{orderNumber}</ValueList>
                </FilterList>
            </ReadOrderRequest>,
            Some(userToken))

    private def readProducts(productIds: List[Long], userToken: String) = {
        val prods = productIds.map { i => <ValueList>{i}</ValueList> }
        wrap(
            <ReadProductRequest xmlns={urn}>
                <DetailSize>Large</DetailSize>
                <FilterList>
                    <Field>ProductId</Field>
                    <Operator>In</Operator>
                    {prods}
                </FilterList>
            </ReadProductRequest>,
            Some(userToken))
    }

    private def wrap(body: NodeSeq, userToken: Option[String] = None): Request = {
        val content =
            <soap12:Envelope
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                    xmlns:soap12="http://www.w3.org/2003/05/soap-envelope">
                <soap12:Header>
                    <SecurityCredential xmlns={urn}>
                        <Application>{application}</Application>
                        <Certificate>{certificate}</Certificate>
                        <UserToken>{userToken.getOrElse("")}</UserToken>
                    </SecurityCredential>
                </soap12:Header>
                <soap12:Body>{body}</soap12:Body>
            </soap12:Envelope>

        endpoint.<<(content.toString, "application/soap+xml; charset=utf-8")
    }


    def receive = {

        case msg @ FetchUserKey(successUrl, failureUrl) =>
            val channel: Channel[FetchUserKeyResponse] = self.channel

            client(getUserKey(successUrl, failureUrl) <> { res =>
                logger.debug("GetUserKey = {}", res)
                (res \\ "Status").head.text match {
                    case "Success" =>
                        val loginUrl = (res \\ "LoginUrl").text
                        val userKey = (res \\ "UserKey").text
                        logger.debug("Successfully fetched LoginUrl {} and UserKey {}", loginUrl, userKey)
                        channel ! FetchUserKeyResponse(msg, Right(FetchUserKeyEnvelope(loginUrl, userKey)))

                    case "Failure" =>
                        logger.error("Error fetching user key %s" format res)
                        channel ! FetchUserKeyResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching user key")))
                }
            })


        case msg @ FetchUserToken(userKey) =>
            val channel: Channel[FetchUserTokenResponse] = self.channel

            client(getUserToken(userKey) <> { res =>
                logger.debug("GetUserToken = {}", res)
                (res \\ "Status").head.text match {
                    case "Success" =>
                        val userToken = (res \\ "UserToken").last.text
                        val expiresOn = xmlDateTimeFormat.parseDateTime((res \\ "Expiration").text).toDate
                        logger.debug("Successfully fetched UserToken {}, ExpiresOn {}", userToken, expiresOn)

                        client(readSiteSettings(userToken) <> { res =>
                            (res \\ "Status").head.text match {
                                case "Success" =>
                                    val companyName = (res \\ "CompanyName").text
                                    val storeUrl = (res \\ "StoreUrl").last.text
                                    val storeSecureUrl = (res \\ "StoreSecureUrl").text
                                    logger.debug("Successfully fetched StoreUrl {} for CompanyName {}", storeUrl, companyName)
                                    channel ! FetchUserTokenResponse(
                                        msg,
                                        Right(FetchUserTokenEnvelope(
                                            userToken,
                                            expiresOn,
                                            companyName,
                                            storeUrl,
                                            storeSecureUrl)))

                                case "Failure" =>
                                    logger.error("Error fetching site settings with user token %s: %s" format(userToken, res))
                                    channel ! FetchUserTokenResponse(
                                        msg,
                                        Left(NetworkSolutionsPartnerException("Error fetching site settings")))
                            }
                        })

                    case "Failure" =>
                        logger.error("Error fetching user token for user key %s: %s" format(userKey, res))
                        channel ! FetchUserTokenResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching user token for user key")))
                }
            })



        case msg @ FetchOrder(userToken, orderNumber) =>
            val channel: Channel[FetchOrderResponse] = self.channel

            client(readOrder(orderNumber, userToken) <> { res =>
                logger.debug("ReadOrder = {}", res)
                (res \\ "Status").head.text match {
                    case "Success" =>
                        val productIds = (res \\ "ProductId").map(_.text).map(java.lang.Long.parseLong(_)).toList
                        val customerNumber = (res \\ "Customer").head.attribute("CustomerNumber").get.head.text
                        val createDate = xmlDateTimeFormat.parseDateTime((res \\ "CreateDate").head.text).toDate
                        logger.debug("ReadOrder {} with ProductIds {}", orderNumber, productIds)

                        client(readProducts(productIds, userToken) <> { res =>
                            (res \\ "Status").head.text match {
                                case "Success" =>
                                    val products = (res \\ "ProductList")

                                    channel ! FetchOrderResponse(
                                        msg,
                                        Right(EchoRequest(
                                            orderNumber.toString,
                                            customerNumber,
                                            createDate,
                                            products.map { p => EchoItem(
                                                productId = p.attribute("ProductId").get.head.text,
                                                productName = (p \ "Name").head.text,
                                                category = (p \ "CategoryList" \ "Name").map(_.text).filter(_.length == 0).headOption.orNull,
                                                brand = null,
                                                price = java.lang.Float.parseFloat((p \ "Price" \ "BasePrice").text),
                                                imageUrl = (p \ "ImageList" \ "DisplayUrl").head.text,
                                                landingPageUrl = (p \ "PageUrl").text,
                                                description = (p \ "Description").text)
                                            }.toList)))

                                case "Failure" =>
                                    logger.error("Error fetching products for order %s with user token %s: %s".format(orderNumber, userToken, res))
                                    channel ! FetchOrderResponse(
                                        msg,
                                        Left(NetworkSolutionsPartnerException("Error fetching products for order %s" format orderNumber)))
                            }
                        })

                    case "Failure" =>
                        logger.error("Error fetching order %s for user token %s: %s" format(orderNumber, userToken, res))
                        channel ! FetchOrderResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching order %s" format orderNumber)))
                }
            })
    }

}
