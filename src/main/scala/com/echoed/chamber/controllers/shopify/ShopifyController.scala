package com.echoed.chamber.controllers.shopify

import org.springframework.stereotype.Controller
import java.util.ArrayList
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import com.echoed.chamber.services.shopify.ShopifyUserServiceLocator
import com.echoed.chamber.services.shopify.{LocateByTokenResponse, GetOrderResponse, GetShopifyUserResponse}
import com.echoed.chamber.controllers.CookieManager
import com.echoed.chamber.services.partner.PartnerServiceManager
import com.echoed.chamber.services.partner.{RegisterPartnerResponse, PartnerNotActive, PartnerNotFound, PartnerAlreadyExists}
import com.echoed.chamber.domain.{Retailer, RetailerSettings, RetailerUser}


@Controller
@RequestMapping(Array("/shopify"))
class ShopifyController {

    @BeanProperty var shopifyUserServiceLocator: ShopifyUserServiceLocator = _
    @BeanProperty var partnerServiceManager: PartnerServiceManager = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var shopifyIntegrationView: String = _
    @BeanProperty var partnerDashboardView: String = _

    private val logger = LoggerFactory.getLogger(classOf[ShopifyController])

    @RequestMapping(value = Array("/auth"), method = Array(RequestMethod.GET))
    def auth(
                @RequestParam(value = "shop", required = true) shop: String,
                @RequestParam(value = "signature", required = true) signature: String,
                @RequestParam(value = "t", required = true) t: String,
                @RequestParam(value = "timestamp", required = true) timeStamp: String,
                httpServletRequest: HttpServletRequest,
                httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        def error(e: Throwable) = {
            val modelAndView = new ModelAndView("ERROR")
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume()
            modelAndView
        }
        if (continuation.isExpired) {
            logger.debug("Continuation is expired")
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            logger.debug("Attempting to locate Shopify User Service: {} ", shop)
            shopifyUserServiceLocator.locate(shop, signature, t, timeStamp).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case LocateByTokenResponse(_, Left(e)) => error(e)
                    case LocateByTokenResponse(_, Right(shopifyUserService)) =>
                        logger.debug("Received shopify user service")

                        shopifyUserService.getShopifyUser.onComplete(_.value.get.fold(
                            error(_),
                            _ match {
                                case GetShopifyUserResponse(_, Left(e)) => error(e)
                                case GetShopifyUserResponse(_, Right(shopifyUser)) =>
                                    val partner = new Retailer(shopifyUser)
                                    val partnerSettings = RetailerSettings.createFutureRetailerSettings(shopifyUser.partnerId)
                                    val partnerUser = new RetailerUser(shopifyUser)
                                    partnerServiceManager.registerPartner(partner,partnerSettings,partnerUser).onComplete(_.value.get.fold(
                                        error(_),
                                        _ match {
                                            case RegisterPartnerResponse(_, Right(pu)) =>
                                                val modelAndView = new ModelAndView(shopifyIntegrationView)
                                                modelAndView.addObject("partner", partner)
                                                modelAndView.addObject("partnerSettings", partnerSettings)
                                                modelAndView.addObject("partnerUser", partnerUser)
                                                continuation.setAttribute("modelAndView", modelAndView)
                                                continuation.resume()
                                            case RegisterPartnerResponse(_, Left(e: PartnerNotActive)) =>
                                                val modelAndView = new ModelAndView(shopifyIntegrationView)
                                                modelAndView.addObject("partner", partner)
                                                modelAndView.addObject("partnerSettings", partnerSettings)
                                                modelAndView.addObject("partnerUser", partnerUser)
                                                continuation.setAttribute("modelAndView", modelAndView)
                                                continuation.resume();
                                            case RegisterPartnerResponse(_, Left(e: PartnerAlreadyExists)) =>
                                                cookieManager.addPartnerUserCookie(httpServletResponse,partnerUser,httpServletRequest)
                                                continuation.setAttribute("modelAndView", new ModelAndView(partnerDashboardView))
                                                continuation.resume();
                                        }))
                            }))
                }))
            continuation.undispatch()
        }
    }


}
