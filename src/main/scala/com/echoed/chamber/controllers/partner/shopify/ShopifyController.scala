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
import org.springframework.web.context.request.async.DeferredResult


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

        val result = new DeferredResult(new ModelAndView("error"))

        logger.debug("Attempting to locate Shopify User Service: {} ", shop)
        shopifyPartnerServiceManager.registerShopifyPartner(shop, signature, t, timeStamp).onSuccess {
            case RegisterShopifyPartnerResponse(_, Right(envelope)) =>
                val modelAndView = new ModelAndView(shopifyIntegrationView)
                modelAndView.addObject("shopifyPartner", envelope.shopifyPartner)
                modelAndView.addObject("partner", envelope.partner)
                modelAndView.addObject("partnerUser", envelope.partnerUser)
                result.set(modelAndView)
            case RegisterShopifyPartnerResponse(_, Left(e: PartnerAlreadyExists)) =>
                result.set(new ModelAndView(partnerLoginView))
        }

        result
    }

}
