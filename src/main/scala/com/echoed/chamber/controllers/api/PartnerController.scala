package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.facebook.{FacebookService, FacebookServiceLocator}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}
import com.echoed.util.CookieManager
import com.echoed.chamber.services.echo.EchoService
import org.springframework.web.servlet.ModelAndView
import scala.collection.JavaConversions
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod,ResponseBody,PathVariable}


@Controller
@RequestMapping(Array("/partner"))
class PartnerController {

    private val logger = LoggerFactory.getLogger(classOf[PartnerController])

    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var partnerLoginErrorView: String = _
    @BeanProperty var partnerLoginView: String = _

    @BeanProperty var partnerDashboardErrorView: String = _
    @BeanProperty var partnerDashboardView: String = _


//    @RequestMapping(value = Array("/login"))
//    def login(
//            @RequestParam(value="email", required=false) email: String,
//            @RequestParam(value="password", required= false) password: String,
//            httpServletRequest: HttpServletRequest,
//            httpServletResponse: HttpServletResponse) = {
//
//
//        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
//        if (continuation.isExpired) {
//            logger.error("Request expired to login {}", email)
//            new ModelAndView(partnerLoginErrorView)
//        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
//            continuation.suspend(httpServletResponse)
//
//            if(email != null && password != null) {
//                def onError(error: PartnerUserException) {
//                    logger.debug("Got error during login for {}: {}", email, error.message)
//                    continuation.setAttribute(
//                        "modelAndView",
//                        new ModelAndView(partnerLoginErrorView, "error", error))
//                    continuation.resume()
//                }
//
//                logger.debug("Received login request for {}", email)
//
//                partnerUserServiceLocator.login(email, password).onResult {
//                    case LoginResponse(_, Left(error)) => onError(error)
//                    case LoginResponse(_, Right(pus)) => pus.getPartnerUser.onResult {
//                        case GetPartnerUserResponse(_, Left(error)) => onError(error)
//                        case GetPartnerUserResponse(_, Right(pu)) =>
//                            logger.debug("Successful login for {}", email)
//                            cookieManager.addCookie(httpServletResponse, "partnerUser", pu.id)
//                            continuation.setAttribute("modelAndView", new ModelAndView(partnerLoginView))
//                            continuation.resume()
//                        case unknown => throw new RuntimeException("Unknown response %s" format unknown)
//                    }
//                    case unknown => throw new RuntimeException("Unknown response %s" format unknown)
//                }
//            }
//            else{
//                continuation.setAttribute("modelAndView", new ModelAndView(partnerLoginErrorView))
//                continuation.resume()
//            }
//            continuation.undispatch()
//        })
//
//    }

//    @RequestMapping(value = Array("/dashboard"), method = Array(RequestMethod.GET))
//    def dashboard(
//            @CookieValue(value = "partnerUser", required = false) partnerUserId: String,
//            httpServletRequest: HttpServletRequest,
//            httpServletResponse: HttpServletResponse) = {
//
//        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
//        if (continuation.isExpired) {
//            logger.error("Request expired to view dashboard for {}", partnerUserId)
//            new ModelAndView(partnerDashboardErrorView)
//        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
//            continuation.suspend(httpServletResponse)
//
//            def onError(error: PartnerUserException) {
//                logger.debug("Got error showing dashboard for {}: {}", partnerUserId, error.message)
//                continuation.setAttribute(
//                    "modelAndView",
//                    new ModelAndView(partnerDashboardErrorView, "error", error))
//                continuation.resume()
//            }
//
//            logger.debug("Showing dashboard for PartnerUser {}", partnerUserId)
//            partnerUserServiceLocator.locate(partnerUserId).onResult {
//                case LocateResponse(_, Left(error)) => onError(error)
//                case LocateResponse(_, Right(pus)) => pus.getPartnerUser.onResult {
//                    case GetPartnerUserResponse(_, Left(error)) => onError(error)
//                    case GetPartnerUserResponse(_, Right(pu)) =>
//                        logger.debug("Got {}", pu)
//                        continuation.setAttribute(
//                            "modelAndView",
//                            new ModelAndView(partnerDashboardView, "partnerUser", pu))
//                        continuation.resume()
//                    case unknown => throw new RuntimeException("Unknown response %s" format unknown)
//                }
//                case unknown => throw new RuntimeException("Unknown response %s" format unknown)
//            }
//
//            continuation.undispatch()
//        })
//
//    }

    @RequestMapping(value = Array("/products/{id}/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getJSON(
            @CookieValue(value = "partnerUser", required = false) partnerUserId: String,
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

            partnerUserServiceLocator.locate(partnerUserId).onResult({
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
                        case _ =>
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
                              @CookieValue(value="partnerUser", required = false) partnerUserId: String,
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

            partnerUserServiceLocator.locate(partnerUserId).onResult({
                case LocateResponse(_, Left(e)) =>
                    logger.error("Error Receiving Partner User Service with PartnerUserId: {}", partnerUserId)
                    error(e)
                case LocateResponse(_, Right(pus)) =>
                    logger.debug("Getting Product Summary for customerId: {}", customerId)
                    query match {
                        case "summary" =>
                            pus.getCustomerSocialSummary(customerId).onResult({
                                case GetCustomerSocialSummaryResponse(_, Left(e)) =>
                                    logger.error("Error getting Retailer Top Products {}", e)
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
                        case "comments" =>
                        case _ =>
                            continuation.setAttribute("jsonResponse", "Invalid Request")
                            continuation.resume()
                    }
            })

            continuation.undispatch()
        })
    }

    @RequestMapping(value = Array("/retailer/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getRetailerJSON(
                           @PathVariable(value="query") query: String,
                           @CookieValue(value="partnerUser", required = false) partnerUserId: String,
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


            partnerUserServiceLocator.locate(partnerUserId).onResult({
                case LocateResponse(_, Left(e)) =>
                    logger.error("Error Receiving Partner User Service with PartnerUserId: {}", partnerUserId)
                    error(e)
                case LocateResponse(_, Right(pus)) =>
                    query match {
                        case "summary" =>
                            pus.getRetailerSocialSummary.onResult({
                                case GetRetailerSocialSummaryResponse(_, Left(e)) =>
                                    logger.error("Error getting Retailer Social Summary", e)
                                    error(e)
                                case GetRetailerSocialSummaryResponse(_, Right(retailerSocialSummary)) =>
                                    logger.debug("Retailer Social Summary: {}", retailerSocialSummary)
                                    continuation.setAttribute("jsonResponse",retailerSocialSummary)
                                    continuation.resume()
                            })
                        case "history" =>
                            pus.getRetailerSocialActivityByDate.onResult({
                                case GetRetailerSocialActivityByDateResponse(_, Left(e)) =>
                                    logger.error("Error getting Retailer Social Activity History")
                                    error(e)
                                case GetRetailerSocialActivityByDateResponse(_, Right(retailerSocialActivity)) =>
                                    continuation.setAttribute("jsonResponse", retailerSocialActivity)
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
                                    continuation.resume
                            })
                        case _ =>
                    }
            })

            continuation.undispatch()
        })
    }


}
