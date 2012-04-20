package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{CookieValue, RequestMapping, RequestMethod,ResponseBody,PathVariable}
import com.echoed.chamber.controllers.CookieManager


@Controller
@RequestMapping(Array("/partner"))
class PartnerController {

    private val logger = LoggerFactory.getLogger(classOf[PartnerController])

    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _
    @BeanProperty var cookieManager: CookieManager = _

    @RequestMapping(value = Array("/products/{id}/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getJSON(
            @PathVariable(value = "id") productId: String,
            @PathVariable(value = "query") query: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) {
            continuation.setAttribute("jsonResponse", e)
            continuation.resume()
        }

        if(continuation.isExpired){
            logger.error("Request expired getting Product Report: {} for Product Id: {}", query, productId)
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            val partnerUserId = cookieManager.findPartnerUserCookie(httpServletRequest)

            partnerUserServiceLocator.locate(partnerUserId.get).onResult({
                case LocateResponse(_, Left(e)) =>
                    logger.error("Error Receiving Partner USer SErvice With PartnerUserId: {}", partnerUserId)
                    error(e)
                case LocateResponse(_, Right(pus)) =>
                    logger.debug("Partner User Service Located with PartnerUserId: {}", partnerUserId)
                    query match{
                        case "history" =>
                            pus.getProductSocialActivityByDate(productId).onResult({
                                case GetProductSocialActivityByDateResponse(_, Left(e)) =>
                                    logger.error("Error getting Activity History for Product: {}", productId)
                                    error(e)
                                case GetProductSocialActivityByDateResponse(_, Right(socialActivityHistory)) =>
                                    continuation.setAttribute("jsonResponse", socialActivityHistory)
                                    continuation.resume()
                            })
                        case "summary" =>
                            pus.getProductSocialSummary(productId).onResult({
                                case GetProductSocialSummaryResponse(_, Left(e)) =>
                                    logger.error("Error getting Product Social Summary for Product: {}", productId)
                                    error(e)
                                case GetProductSocialSummaryResponse(_, Right(productSocialSummary))=>
                                    continuation.setAttribute("jsonResponse", productSocialSummary)
                                    continuation.resume()
                            })
                        case "comments" =>
                            pus.getCommentsByProductId(productId).onResult({
                                case GetCommentsByProductIdResponse(_, Left(e)) =>
                                    logger.error("Error getting comments for Product: {}", productId)
                                    error(e)
                                case GetCommentsByProductIdResponse(_, Right(productComments)) =>
                                    continuation.setAttribute("jsonResponse", productComments)
                                    continuation.resume()
                            })
                        case _ => error(new IllegalArgumentException("Invalid Request"))
                    }
            })
            continuation.undispatch();

        })

    }

    @RequestMapping(value = Array("/customers/{id}/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getCustomerJSON(
            @PathVariable(value="id") customerId: String,
            @PathVariable(value="query") query: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation((httpServletRequest))

        def error(e: Throwable) {
            continuation.setAttribute("jsonResponse", e)
            continuation.resume()
        }

        if(continuation.isExpired){
            logger.error("Request expired getting product social summary json")
            continuation.setAttribute("jsonResponse", "Request Expired")
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            val partnerUserId = cookieManager.findPartnerUserCookie(httpServletRequest)

            partnerUserServiceLocator.locate(partnerUserId.get).onResult({
                case LocateResponse(_, Left(e)) =>
                    logger.error("Error Receiving Partner User Service with PartnerUserId: {}", partnerUserId)
                    error(e)
                case LocateResponse(_, Right(pus)) =>
                    logger.debug("Getting Product Summary for customerId: {}", customerId)
                    query match {
                        case "summary" =>
                            pus.getCustomerSocialSummary(customerId).onResult({
                                case GetCustomerSocialSummaryResponse(_, Left(e)) =>
                                    logger.error("Error getting Partner Top Products {}", e)
                                    error(e)
                                case GetCustomerSocialSummaryResponse(_, Right(customerSocialSummary)) =>
                                    logger.debug("Customer Social Summary: {}", customerSocialSummary)
                                    continuation.setAttribute("jsonResponse",customerSocialSummary)
                                    continuation.resume()
                            })
                        case "history" =>
                            pus.getCustomerSocialActivityByDate(customerId).onResult({
                                case GetCustomerSocialActivityByDateResponse(_, Left(e)) =>
                                    error(e)
                                case GetCustomerSocialActivityByDateResponse(_, Right(socialActivityHistory)) =>
                                    continuation.setAttribute("jsonResponse", socialActivityHistory)
                                    continuation.resume()
                            })
                        case "comments" => error(new IllegalArgumentException("Invalid Request"))
                        case _ => error(new IllegalArgumentException("Invalid Request"))
                    }
            })

            continuation.undispatch()
        })
    }

    @RequestMapping(value = Array("/reports/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerJSON(
            @PathVariable(value="query") query: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation((httpServletRequest))

        def error(e: Throwable) {
            continuation.setAttribute("jsonResponse", e)
            continuation.resume()
        }

        if(continuation.isExpired){
            logger.error("Request expired getting product social summary json")
            continuation.setAttribute("jsonResponse", "Request Expired")
            continuation.resume()
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            val partnerUserId = cookieManager.findPartnerUserCookie(httpServletRequest)

            partnerUserServiceLocator.locate(partnerUserId.get).onResult({
                case LocateResponse(_, Left(e)) =>
                    logger.error("Error Receiving Partner User Service with PartnerUserId: {}", partnerUserId)
                    error(e)
                case LocateResponse(_, Right(pus)) =>
                    query match {
                        case "summary" =>
                            pus.getPartnerSocialSummary.onResult({
                                case GetPartnerSocialSummaryResponse(_, Left(e)) =>
                                    logger.error("Error getting Partner Social Summary", e)
                                    error(e)
                                case GetPartnerSocialSummaryResponse(_, Right(partnerSocialSummary)) =>
                                    logger.debug("Partner Social Summary: {}", partnerSocialSummary)
                                    continuation.setAttribute("jsonResponse", partnerSocialSummary)
                                    continuation.resume()
                            })
                        case "echoes" =>
                            pus.getEchoes.onResult({
                                case GetEchoesResponse(_, Left(e)) =>
                                    logger.error("Error getting Echoes", e)
                                    error(e)
                                case GetEchoesResponse(_, Right(echoes)) =>
                                    continuation.setAttribute("jsonResponse", echoes)
                                    continuation.resume()
                            })
                        case "history" =>
                            pus.getPartnerSocialActivityByDate.onResult({
                                case GetPartnerSocialActivityByDateResponse(_, Left(e)) =>
                                    logger.error("Error getting Partner Social Activity History")
                                    error(e)
                                case GetPartnerSocialActivityByDateResponse(_, Right(partnerSocialActivity)) =>
                                    continuation.setAttribute("jsonResponse", partnerSocialActivity)
                                    continuation.resume()
                            })
                        case "comments" =>
                            pus.getComments.onResult({
                                case GetCommentsResponse(_, Left(e)) =>
                                    error(e)
                                case GetCommentsResponse(_, Right(comments)) =>
                                    continuation.setAttribute("jsonResponse", comments)
                                    continuation.resume()
                            })
                        case "customers" =>
                            pus.getCustomers.onResult({
                                case GetCustomersResponse(_, Left(e))=>
                                    error(e)
                                case GetCustomersResponse(_, Right(customers)) =>
                                    continuation.setAttribute("jsonResponse", customers )
                                    continuation.resume()
                            })
                        case "topcustomers" =>
                            pus.getTopCustomers.onResult({
                                case GetTopCustomersResponse(_,Left(e))=>
                                    error(e)
                                case GetTopCustomersResponse(_, Right(customers)) =>
                                    continuation.setAttribute("jsonResponse", customers)
                                    continuation.resume()
                            })
                        case "products" =>
                            pus.getProducts.onResult({
                                case GetProductsResponse(_, Left(e)) =>
                                    error(e)
                                case GetProductsResponse(_, Right(products))=>
                                    continuation.setAttribute("jsonResponse", products)
                                    continuation.resume()
                            })
                        case "topproducts" =>
                            pus.getTopProducts.onResult({
                                case GetTopProductsResponse(_, Left(e)) =>
                                    error(e)
                                case GetTopProductsResponse(_, Right(products)) =>
                                    continuation.setAttribute("jsonResponse", products)
                                    continuation.resume()
                            })
                        case "geolocation" =>
                            pus.getEchoClickGeoLocation.onResult({
                                case GetEchoClickGeoLocationResponse(_, Left(e)) =>
                                    error(e)
                                case GetEchoClickGeoLocationResponse(_, Right(geolocation)) =>
                                    continuation.setAttribute("jsonResponse",geolocation)
                                    continuation.resume()
                            })
                        case _ => error(new IllegalArgumentException("Invalid Request"))
                    }
            })

            continuation.undispatch()
        })
    }


}
