package com.echoed.chamber.services.partner.networksolutions


import scalaxb.{DataRecord, SoapClients, DispatchHttpClients, Fault}
import com.echoed.networksolutions.ecomapi._


object ecomapiTest extends App {


    val application = "Echoed Dev"
    val certificate = "0d7ae74afe11457cb2b2267ff13d2d06"

    val userKey = "w6E5DcJk42Wng7KH"
    val userToken = "r9R3Eqz7LQe86ZgNm42TpYf5w9MBc7s3"

    val orderId = 1L
    val productId = 1L

    val client = new NetSolEcomServiceSoap12Bindings with SoapClients with DispatchHttpClients {
        //this does not actually need to be here but for some reason IntelliJ says it does :(
        override def baseAddress = new java.net.URI("https://ecomapi.networksolutions.com/SoapService.asmx")
    }



//    val readOrder = client.service.readOrder(
//        new ReadOrderRequestType(
//            DetailSize = Some(Large),
//            FilterList = List[FilterType](new FilterType(
//                Field = Some("OrderId"),
//                Operator = Some(Equal),
//                ValueList = List[String](orderId.toString)))),
//        new SecurityCredentialType(
//            Application = Some(application),
//            Certificate = Some(certificate),
//            attributes = Map[String, DataRecord[Any]](),
//            UserToken = Some(userToken)))
//
//    println(readOrder);


    val readProduct = client.service.readProduct(
        new ReadProductRequestType(
            DetailSize = Some(Large),
            FilterList = List[FilterType](new FilterType(
                Field = Some("ProductId"),
                Operator = Some(Equal),
                ValueList = List[String](productId.toString)))),
        new SecurityCredentialType(
            Application = Some(application),
            Certificate = Some(certificate),
            attributes = Map[String, DataRecord[Any]](),
            UserToken = Some(userToken)))

    println(readProduct);




    //get the user token and forward them on to the resulting login url
//    val getUserKey = client.service.getUserKey(
//        new GetUserKeyRequestType(UserKey = Some(new UserKeyType(
//                SuccessUrl = Some("http://localhost:8080/partner/login"),
//                FailureUrl = Some("http://localhost:8080/")))),
//        new SecurityCredentialType(
//            Application = Some(application),
//            Certificate = Some(certificate),
//            attributes = Map[String, DataRecord[Any]]()));
//
//    println(getUserKey)
//
//
//    getUserKey.fold(
//        e => throw new RuntimeException("Error getting user key %s" format(e)),
//        response => {
//            println("Now redirect the user to %s" format response.UserKey.get.LoginUrl)
//            println("The user key is %s" format response.UserKey.get.UserKey)
//            println("The success url is %s" format response.UserKey.get.SuccessUrl)
//
//            //now we should have some success post back... get the user token
//            val getUserToken = client.service.getUserToken(
//                new GetUserTokenRequestType(UserToken = Some(UserTokenType(UserKey = Some(response.UserKey.get.UserKey.get)))),
//                new SecurityCredentialType(
//                    Application = Some(application),
//                    Certificate = Some(certificate),
//                    attributes = Map[String, DataRecord[Any]]()))
//
//            println(getUserToken)
//
//            getUserToken.fold(
//                e => throw new RuntimeException("Error getting user token %s" format(e)),
//                response => {
//                    println("The user token is %s" format response.UserToken.get.UserToken)
//                    println("The user key is %s" format response.UserToken.get.UserKey)
//                    println("The expiration is %s" format response.UserToken.get.Expiration)
//
//                    val readProduct = client.service.readProduct(
//                        new ReadProductRequestType(),
//                        new SecurityCredentialType(
//                            Application = Some(application),
//                            Certificate = Some(certificate),
//                            attributes = Map[String, DataRecord[Any]](),
//                            UserToken = Some(response.UserToken.get.UserToken.get)))
//
//                    println(readProduct);
//                })
//        })

}

/*

INF: [console logger] dispatch: ecomapi.networksolutions.com POST /SoapService.asmx HTTP/1.1
Right(GetUserKeyResponseType(None,Success,2012-05-04T17:08:15.1460478Z,None,List(),None,Some(UserKeyType(Some(https://ecomapi.networksolutions.com/Authorize.aspx?userkey=w6E5DcJk42Wng7KH),Some(w6E5DcJk42Wng7KH),None,None))))
Now redirect the user to Some(https://ecomapi.networksolutions.com/Authorize.aspx?userkey=w6E5DcJk42Wng7KH)
The user key is Some(w6E5DcJk42Wng7KH)
The success url is None
INF: [console logger] dispatch: ecomapi.networksolutions.com POST /SoapService.asmx HTTP/1.1
Right(GetUserTokenResponseType(None,Success,2012-05-04T17:09:24.5692557Z,None,List(),None,Some(UserTokenType(None,Some(r9R3Eqz7LQe86ZgNm42TpYf5w9MBc7s3),Some(2013-02-03T17:09:13.083)))))
The user token is Some(r9R3Eqz7LQe86ZgNm42TpYf5w9MBc7s3)
The user key is None
The expiration is Some(2013-02-03T17:09:13.083)
INF: [console logger] dispatch: ecomapi.networksolutions.com POST /SoapService.asmx HTTP/1.1

*/

