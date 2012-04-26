package com.echoed.chamber.controllers.partner.shopify

import org.springframework.stereotype.Controller
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.controllers.CookieManager
import com.echoed.chamber.services.partner.{PartnerNotActive, PartnerAlreadyExists}
import com.echoed.chamber.services.partner.shopify.{RegisterShopifyPartnerEnvelope, RegisterShopifyPartnerResponse, ShopifyPartnerServiceManager}


@Controller
@RequestMapping(Array("/shopify"))
class ShopifyController {

    @BeanProperty var shopifyPartnerServiceManager: ShopifyPartnerServiceManager = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var shopifyIntegrationView: String = _
    @BeanProperty var partnerLoginView: String = _

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

        def makeView(envelope: RegisterShopifyPartnerEnvelope) {
            val modelAndView = new ModelAndView(shopifyIntegrationView)
            modelAndView.addObject("shopifyPartner", envelope.shopifyPartner)
            modelAndView.addObject("partner", envelope.partner)
            modelAndView.addObject("partnerUser", envelope.partnerUser)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume()
        }

        if (continuation.isExpired) {
            logger.debug("Continuation is expired")
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            logger.debug("Attempting to locate Shopify User Service: {} ", shop)
            shopifyPartnerServiceManager.registerShopifyPartner(shop, signature, t, timeStamp).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case RegisterShopifyPartnerResponse(_, Right(envelope)) => makeView(envelope)
                    case RegisterShopifyPartnerResponse(_, Left(e: PartnerNotActive)) => error(e)
                    case RegisterShopifyPartnerResponse(_, Left(e: PartnerAlreadyExists)) =>
                        continuation.setAttribute("modelAndView", new ModelAndView(partnerLoginView))
                        continuation.resume()
                    case RegisterShopifyPartnerResponse(_, Left(e)) => error(e)

                }))

            continuation.undispatch()
        }
    }


}
