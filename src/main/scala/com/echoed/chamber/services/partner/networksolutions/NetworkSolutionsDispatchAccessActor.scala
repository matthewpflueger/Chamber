package com.echoed.chamber.services.partner.networksolutions

import org.joda.time.format.ISODateTimeFormat

import dispatch._

import com.echoed.chamber.services.partner.{EchoItem, EchoRequest}
import akka.actor._
import com.ning.http.client.RequestBuilder
import xml._
import com.echoed.chamber.services.EchoedActor


class NetworkSolutionsDispatchAccessActor(
        application: String,
        certificate: String,
        client: Http) extends EchoedActor {


    private final val xmlDateTimeFormat = ISODateTimeFormat.dateTimeNoMillis

    val urn = "urn:networksolutions:apis"
    val endpoint = "https://ecomapi.networksolutions.com/SoapService.asmx"

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


    private def wrap(body: NodeSeq, userToken: Option[String] = None): RequestBuilder = {
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

        url(endpoint)
            .addHeader("Accept", "application/soap+xml; charset=utf-8")
            .setMethod("POST")
            .setBody(content.toString)
    }


    def asXml(request: RequestBuilder)
            (callback: Elem => Unit)
            (error: PartialFunction[Throwable, Unit]) {
        client(request OK As.string).onSuccess {
            case s => callback(XML.loadString(s))
        }.onFailure {
            case e => error(e)
        }
    }


    def handle = {

        case msg @ FetchUserKey(successUrl, failureUrl) =>
            val channel = context.sender

            asXml(getUserKey(successUrl, failureUrl)) { res =>
//            client(getUserKey(successUrl, failureUrl) <> { res =>
                log.debug("GetUserKey = {}", res)
                (res \\ "Status").head.text match {
                    case "Success" =>
                        val loginUrl = (res \\ "LoginUrl").text
                        val userKey = (res \\ "UserKey").text
                        log.debug("Successfully fetched LoginUrl {} and UserKey {}", loginUrl, userKey)
                        channel ! FetchUserKeyResponse(msg, Right(FetchUserKeyEnvelope(loginUrl, userKey)))

                    case "Failure" =>
                        log.error("Error fetching user key {}", res)
                        channel ! FetchUserKeyResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching user key")))
                }
            } {
                case e => log.error("Unexpected error fetching user key {}", e)
            }//)


        case msg @ FetchUserToken(userKey) =>
            val channel = context.sender

            asXml(getUserToken(userKey)) { res =>
//            client(getUserToken(userKey) <> { res =>
                log.debug("GetUserToken = {}", res)
                (res \\ "Status").head.text match {
                    case "Success" =>
                        val userToken = (res \\ "UserToken").last.text
                        val expiresOn = xmlDateTimeFormat.parseDateTime((res \\ "Expiration").text).toDate
                        log.debug("Successfully fetched UserToken {}, ExpiresOn {}", userToken, expiresOn)

                        asXml(readSiteSettings(userToken)) { res =>
//                        client(readSiteSettings(userToken) <> { res =>
                            (res \\ "Status").head.text match {
                                case "Success" =>
                                    val companyName = (res \\ "CompanyName").text
                                    val storeUrl = (res \\ "StoreUrl").last.text
                                    val storeSecureUrl = (res \\ "StoreSecureUrl").text
                                    log.debug("Successfully fetched StoreUrl {} for CompanyName {}", storeUrl, companyName)
                                    channel ! FetchUserTokenResponse(
                                        msg,
                                        Right(FetchUserTokenEnvelope(
                                            userToken,
                                            expiresOn,
                                            companyName,
                                            storeUrl,
                                            storeSecureUrl)))

                                case "Failure" =>
                                    log.error("Error fetching site settings with user token {}: {}", userToken, res)
                                    channel ! FetchUserTokenResponse(
                                        msg,
                                        Left(NetworkSolutionsPartnerException("Error fetching site settings")))
                            }
                        } {
                            case e => log.error("Unexpected error fetching site settings with user token {}: {}", userToken, e)
                        }

                    case "Failure" =>
                        log.error("Error fetching user token for user key {}: {}", userKey, res)
                        channel ! FetchUserTokenResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching user token for user key")))
                }
            } {
                case e => log.error("Unexpected error fetching user token for user key {}: {}", userKey, e)
            }



        case msg @ FetchOrder(userToken, orderNumber) =>
            val channel = context.sender

            asXml(readOrder(orderNumber, userToken)) { res =>
//            client(readOrder(orderNumber, userToken) <> { res =>
                log.debug("ReadOrder = {}", res)
                (res \\ "Status").head.text match {
                    case "Success" =>
                        val productIds = (res \\ "ProductId").map(_.text).map(java.lang.Long.parseLong(_)).toList
                        val customerNumber = (res \\ "Customer").head.attribute("CustomerNumber").get.head.text
                        val createDate = xmlDateTimeFormat.parseDateTime((res \\ "CreateDate").head.text).toDate
                        log.debug("ReadOrder {} with ProductIds {}", orderNumber, productIds)

                        asXml(readProducts(productIds, userToken)) { res =>
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
                                    log.error("Error fetching products for order {} with user token {}: {}", orderNumber, userToken, res)
                                    channel ! FetchOrderResponse(
                                        msg,
                                        Left(NetworkSolutionsPartnerException("Error fetching products for order %s" format orderNumber)))
                            }
                        } {
                            case e => log.error("Unexpected error fetching products for order{} with user token{}: {}", orderNumber, userToken, e)
                        }

                    case "Failure" =>
                        log.error("Error fetching order {} for user token {}: {}", orderNumber, userToken, res)
                        channel ! FetchOrderResponse(
                            msg,
                            Left(NetworkSolutionsPartnerException("Error fetching order %s" format orderNumber)))
                }
            } {
                case e => log.error("Unexpected error fetching order {} for user token {}: {}", orderNumber, userToken, e)
            }
    }

}
