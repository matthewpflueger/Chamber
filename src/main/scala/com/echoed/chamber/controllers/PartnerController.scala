package com.echoed.chamber.controllers

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



    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam("email") email: String,
            @RequestParam("password") password: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {


        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to login {}", email)
            new ModelAndView(partnerLoginErrorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            def onError(error: PartnerUserException) {
                logger.debug("Got error during login for {}: {}", email, error.message)
                continuation.setAttribute(
                    "modelAndView",
                    new ModelAndView(partnerLoginErrorView, "error", error))
                continuation.resume()
            }

            logger.debug("Received login request for {}", email)

            partnerUserServiceLocator.login(email, password).onResult {
                case LoginResponse(_, Left(error)) => onError(error)
                case LoginResponse(_, Right(pus)) => pus.getPartnerUser.onResult {
                    case GetPartnerUserResponse(_, Left(error)) => onError(error)
                    case GetPartnerUserResponse(_, Right(pu)) =>
                        logger.debug("Successful login for {}", email)
                        cookieManager.addCookie(httpServletResponse, "partnerUser", pu.id)
                        continuation.setAttribute("modelAndView", new ModelAndView(partnerLoginView))
                        continuation.resume()
                    case unknown => throw new RuntimeException("Unknown response %s" format unknown)
                }
                case unknown => throw new RuntimeException("Unknown response %s" format unknown)
            }

            continuation.undispatch()
        })

    }

    @RequestMapping(value = Array("/dashboard"), method = Array(RequestMethod.GET))
    def dashboard(
            @CookieValue(value = "partnerUser", required = false) partnerUserId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view dashboard for {}", partnerUserId)
            new ModelAndView(partnerDashboardErrorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            def onError(error: PartnerUserException) {
                logger.debug("Got error showing dashboard for {}: {}", partnerUserId, error.message)
                continuation.setAttribute(
                    "modelAndView",
                    new ModelAndView(partnerDashboardErrorView, "error", error))
                continuation.resume()
            }

            logger.debug("Showing dashboard for PartnerUser {}", partnerUserId)
            partnerUserServiceLocator.locate(partnerUserId).onResult {
                case LocateResponse(_, Left(error)) => onError(error)
                case LocateResponse(_, Right(pus)) => pus.getPartnerUser.onResult {
                    case GetPartnerUserResponse(_, Left(error)) => onError(error)
                    case GetPartnerUserResponse(_, Right(pu)) =>
                        logger.debug("Got {}", pu)
                        continuation.setAttribute(
                            "modelAndView",
                            new ModelAndView(partnerDashboardView, "partnerUser", pu))
                        continuation.resume()
                    case unknown => throw new RuntimeException("Unknown response %s" format unknown)
                }
                case unknown => throw new RuntimeException("Unknown response %s" format unknown)
            }

            continuation.undispatch()
        })

    }

    @RequestMapping(value = Array("/summary"), method = Array(RequestMethod.GET))
    @ResponseBody
    def summary(
            @CookieValue(value="partnerUser", required = false) partnerUserId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            logger.error("Request expired to view dashboard for {}", partnerUserId)
            //new ModelAndView(partnerDashboardErrorView)
        } else Option(continuation.getAttribute("summary")).getOrElse({

            continuation.suspend(httpServletResponse)

            partnerUserServiceLocator.locate(partnerUserId).onResult({
                case LocateResponse(_, Left(error)) =>
                    logger.error("Error Receiving Partner User Service with PartnerUserId: {}", partnerUserId)
                case LocateResponse(_, Right(pus)) =>
                    pus.getRetailerSocialSummary.onResult({
                        case GetRetailerSocialSummaryResponse(_, Left(error)) =>
                            logger.error("Error getting Retailer Social Summary", error)
                        case GetRetailerSocialSummaryResponse(_, Right(retailerSocialSummary)) =>
                            logger.debug("Retailer Social Summary: {}", retailerSocialSummary)
                            if(retailerSocialSummary != null)
                                continuation.setAttribute("summary",retailerSocialSummary)
                            else
                                continuation.setAttribute("summary","fail")
                            continuation.resume()
                    })
            })


            continuation.undispatch()
        })

    }

    @RequestMapping(value = Array("/topproducts"), method = Array(RequestMethod.GET))
    @ResponseBody
    def topProducts(
                   @CookieValue(value="partnerUser", required = false) partnerUserId: String,
                   httpServletRequest: HttpServletRequest,
                   httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            logger.error("Request expired to view dashboard for {}", partnerUserId)
            new ModelAndView(partnerDashboardErrorView)
        } else Option(continuation.getAttribute("topproducts")).getOrElse({

            continuation.suspend(httpServletResponse)

            partnerUserServiceLocator.locate(partnerUserId).onResult({
                case LocateResponse(_, Left(error)) =>
                    logger.error("Error Receiving Partner User Service with PartnerUserId: {}", partnerUserId)
                case LocateResponse(_, Right(pus)) =>
                    pus.getTopProducts.onResult({
                        case GetTopProductsResponse(_, Left(error)) =>
                            logger.error("Error getting Retailer Top Products {}", error)
                        case GetTopProductsResponse(_, Right(retailerTopProductsView)) =>
                            if(retailerTopProductsView == null)
                                continuation.setAttribute("topproducts", "fail")
                            else
                                continuation.setAttribute("topproducts",retailerTopProductsView)
                            continuation.resume()
                    })
            })


            continuation.undispatch()
        })

    }

    @RequestMapping(value = Array("/product/{id}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getProductSummary(
            @PathVariable(value="id") productId: String,
            @CookieValue(value="partnerUser", required = false) partnerUserId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation((httpServletRequest))

        if(continuation.isExpired){
            logger.error("Request expired getting product social summary json")
        } else Option(continuation.getAttribute("productSummary")).getOrElse({
            continuation.suspend(httpServletResponse)

            partnerUserServiceLocator.locate(partnerUserId).onResult({
                case LocateResponse(_, Left(error)) =>
                    logger.error("Error Receiving Partner User Service with PartnerUserId: {}", partnerUserId)
                case LocateResponse(_, Right(pus)) =>
                    logger.debug("Getting Product Summary for productId: {}", productId)
                    pus.getProductSocialSummary(productId).onResult({
                        case GetProductSocialSummaryResponse(_, Left(error)) =>
                            logger.error("Error getting Retailer Top Products {}", error)
                        case GetProductSocialSummaryResponse(_, Right(productSocialSummary)) =>
                            logger.debug("Product Social Summary: {}", productSocialSummary)
                            if(productSocialSummary == null)
                                continuation.setAttribute("productSummary", "fail")
                            else
                                continuation.setAttribute("productSummary",productSocialSummary)
                            continuation.resume()
                    })
            })

            continuation.undispatch()
        })
    }
}
