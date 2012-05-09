package com.echoed.chamber.services.partner.networksolutions

import dispatch.nio.Http
import xml.{NodeSeq, Elem}
import dispatch.{Request, url}
import scala.Some

object DispatcherSoapTest extends App {

//    private def wrap(xml: Elem): String = {
//        val buf = new StringBuilder
//        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
//        buf.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n")
//        buf.append("<SOAP-ENV:Header>
//        buf.append("<SOAP-ENV:Body>\n")
//        buf.append(xml.toString)
//        buf.append("\n</SOAP-ENV:Body>\n")
//        buf.append("</SOAP-ENV:Envelope>\n")
//        buf.toString
//    }

    val h = new Http
    val urn = "urn:networksolutions:apis"
    val endpoint = url("https://ecomapi.networksolutions.com/SoapService.asmx")
    val application = "Echoed Dev"
    val certificate = "0d7ae74afe11457cb2b2267ff13d2d06"
    val successUrl = "http://localhost.com:8080/networksolutions/auth"
    val userToken = "Tf64Rxz8NMt59YcGp7k3Z2JaHi6g7W8P"

    def getSiteSettings(userToken: String) =
        wrap(
            <ReadSiteSettingRequest xmlns={urn}>
                <DetailSize>Large</DetailSize>
            </ReadSiteSettingRequest>,
            Some(userToken))

    def getProducts(productIds: List[Long], userToken: String) = {
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

    def getOrder(orderNumber: Long, userToken: String) =
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
    /*
        Success example: <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"><soap:Body><GetUserKeyResponse xmlns="urn:networksolutions:apis"><Status>Success</Status><TimeStamp>2012-05-09T17:50:52.5486601Z</TimeStamp><UserKey><LoginUrl>https://ecomapi.networksolutions.com/Authorize.aspx?userkey=p8GEs7b3ZHk4j5Q6</LoginUrl><UserKey>p8GEs7b3ZHk4j5Q6</UserKey></UserKey></GetUserKeyResponse></soap:Body></soap:Envelope>
        Failure example: <soap:Envelope xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:soap="http://www.w3.org/2003/05/soap-envelope"><soap:Body><GetUserKeyResponse xmlns="urn:networksolutions:apis"><Status>Failure</Status><TimeStamp>2012-05-09T19:06:31.3858041Z</TimeStamp><ErrorList><Message>Supplied value is invalid.</Message><Number>607</Number><Severity>Error</Severity><FieldInfo><Field>SecurityCredential.Application</Field><Information></Information></FieldInfo></ErrorList></GetUserKeyResponse></soap:Body></soap:Envelope>

     */
    def getUserToken(successUrl: String, failureUrl: Option[String] = None) =
            wrap(
                <GetUserKeyRequest xmlns={urn}>
                    <UserKey>
                        <SuccessUrl>{successUrl}</SuccessUrl>
                        <FailureUrl>{failureUrl.getOrElse("")}</FailureUrl>
                    </UserKey>
                </GetUserKeyRequest>)

    def wrap(body: NodeSeq, userToken: Option[String] = None): Request = {
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



    h(getUserToken(successUrl) <> { res =>
        println("getUserKey: %s" format res)
        (res \\ "Status").head.text match {
            case "Success" =>
                println("LoginUrl: %s" format (res \\ "LoginUrl").text)
                println("UserKey: %s" format (res \\ "UserKey").text)
            case "Failure" => println("FAILURE")

        }
    })

    h(getOrder(1L, userToken) <> { res =>
        println("getOrder: %s" format res)
        (res \\ "Status").head.text match {
            case "Success" =>
                println("ProductIds: %s" format (res \\ "ProductId").map(_.text).mkString(", "))
            case "Failure" => println("FAILURE")
            case other => println("What the?!?! %s" format other)
        }
    })

    h(getProducts(1L :: Nil, userToken) <> { res =>
        println("getProducts: %s" format res)
        (res \\ "Status").head.text match {
            case "Success" =>
                println("DisplayUrls: %s" format (res \\ "DisplayUrl").map(_.text).mkString(", "))
            case "Failure" => println("FAILURE")
            case other => println("What the?!?! %s" format other)
        }
    })

    h(getSiteSettings(userToken) <> { res =>
        println("getSiteSettings: %s" format res)
        (res \\ "Status").head.text match {
            case "Success" =>
                println("StoreUrl: %s" format (res \\ "StoreUrl").text)
                println("StoreSecureUrl: %s" format (res \\ "StoreSecureUrl").text)
            case "Failure" => println("FAILURE")
            case other => println("What the?!?! %s" format other)
        }
    })

    println("Sleeping for 5 seconds")
    Thread.sleep(5000)
    println("Exiting")
    h.shutdown
}
