package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{CookieValue, RequestMapping, RequestMethod,ResponseBody,PathVariable}
import com.echoed.chamber.controllers.CookieManager
import org.springframework.web.context.request.async.DeferredResult


@Controller
@RequestMapping(Array("/partner"))
class PartnerController {

    private val logger = LoggerFactory.getLogger(classOf[PartnerController])

    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _
    @BeanProperty var cookieManager: CookieManager = _

    @RequestMapping(value = Array("/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getSettings(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")
        val partnerUserId = cookieManager.findPartnerUserCookie(httpServletRequest)

        partnerUserServiceLocator.locate(partnerUserId.get).onSuccess {
            case LocateResponse(_, Right(pus)) =>
                pus.getPartnerSettings.onSuccess {
                    case GetPartnerSettingsResponse(_, Right(partnerSettings)) =>
                        logger.debug("Successfully received partnerSettings for PartnerUser {}", partnerUserId)
                        result.set(partnerSettings)
                }
        }

        result
    }

    @RequestMapping(value = Array("/products/{id}/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getJSON(
            @PathVariable(value = "id") productId: String,
            @PathVariable(value = "query") query: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")
        val partnerUserId = cookieManager.findPartnerUserCookie(httpServletRequest)

        partnerUserServiceLocator.locate(partnerUserId.get).onSuccess {
            case LocateResponse(_, Right(pus)) =>
                logger.debug("Partner User Service Located with PartnerUserId: {}", partnerUserId)
                query match {
                    case "history" =>
                        pus.getProductSocialActivityByDate(productId).onSuccess {
                            case GetProductSocialActivityByDateResponse(_, Right(socialActivityHistory)) =>
                                result.set(socialActivityHistory)
                        }
                    case "summary" =>
                        pus.getProductSocialSummary(productId).onSuccess {
                            case GetProductSocialSummaryResponse(_, Right(productSocialSummary))=>
                                result.set(productSocialSummary)
                        }
                    case "comments" =>
                        pus.getCommentsByProductId(productId).onSuccess {
                            case GetCommentsByProductIdResponse(_, Right(productComments)) =>
                                result.set(productComments)
                        }
                }
        }

        result

    }

    @RequestMapping(value = Array("/customers/{id}/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getCustomerJSON(
            @PathVariable(value="id") customerId: String,
            @PathVariable(value="query") query: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")
        val partnerUserId = cookieManager.findPartnerUserCookie(httpServletRequest)

        partnerUserServiceLocator.locate(partnerUserId.get).onSuccess {
            case LocateResponse(_, Right(pus)) =>
                logger.debug("Getting Product Summary for customerId: {}", customerId)
                query match {
                    case "summary" =>
                        pus.getCustomerSocialSummary(customerId).onSuccess {
                            case GetCustomerSocialSummaryResponse(_, Right(customerSocialSummary)) =>
                                logger.debug("Customer Social Summary: {}", customerSocialSummary)
                                result.set(customerSocialSummary)
                        }
                    case "history" =>
                        pus.getCustomerSocialActivityByDate(customerId).onSuccess {
                            case GetCustomerSocialActivityByDateResponse(_, Right(socialActivityHistory)) =>
                                result.set(socialActivityHistory)
                        }
                }
        }

        result
    }

    @RequestMapping(value = Array("/reports/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerJSON(
            @PathVariable(value="query") query: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")
        val partnerUserId = cookieManager.findPartnerUserCookie(httpServletRequest)

            partnerUserServiceLocator.locate(partnerUserId.get).onSuccess {
                case LocateResponse(_, Right(pus)) =>
                    query match {
                        case "summary" =>
                            pus.getPartnerSocialSummary.onSuccess {
                                case GetPartnerSocialSummaryResponse(_, Right(partnerSocialSummary)) =>
                                    logger.debug("Partner Social Summary: {}", partnerSocialSummary)
                                    result.set(partnerSocialSummary)
                            }
                        case "echoes" =>
                            pus.getEchoes.onSuccess {
                                case GetEchoesResponse(_, Right(echoes)) => result.set(echoes)
                            }
                        case "history" =>
                            pus.getPartnerSocialActivityByDate.onSuccess {
                                case GetPartnerSocialActivityByDateResponse(_, Right(partnerSocialActivity)) =>
                                    result.set(partnerSocialActivity)
                            }
                        case "comments" =>
                            pus.getComments.onSuccess{
                                case GetCommentsResponse(_, Right(comments)) => result.set(comments)
                            }
                        case "customers" =>
                            pus.getCustomers.onSuccess {
                                case GetCustomersResponse(_, Right(customers)) => result.set(customers)
                            }
                        case "topcustomers" =>
                            pus.getTopCustomers.onSuccess {
                                case GetTopCustomersResponse(_, Right(customers)) => result.set(customers)
                            }
                        case "products" =>
                            pus.getProducts.onSuccess {
                                case GetProductsResponse(_, Right(products))=> result.set(products)
                            }
                        case "topproducts" =>
                            pus.getTopProducts.onSuccess {
                                case GetTopProductsResponse(_, Right(products)) => result.set(products)
                            }
                        case "geolocation" =>
                            pus.getEchoClickGeoLocation.onSuccess {
                                case GetEchoClickGeoLocationResponse(_, Right(geolocation)) => result.set(geolocation)
                            }
                    }
            }

        result
    }

}
