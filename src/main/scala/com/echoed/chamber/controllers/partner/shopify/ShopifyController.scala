package com.echoed.chamber.controllers.partner.shopify

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partner.PartnerAlreadyExists
import com.echoed.chamber.services.partner.shopify.{RegisterShopifyPartner, RegisterShopifyPartnerResponse}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.EchoedController


@Controller
@RequestMapping(Array("/shopify"))
class ShopifyController extends EchoedController {

    @RequestMapping(value = Array("/auth"), method = Array(RequestMethod.GET))
    def auth(
            @RequestParam(value = "shop", required = true) shop: String,
            @RequestParam(value = "signature", required = true) signature: String,
            @RequestParam(value = "t", required = true) t: String,
            @RequestParam(value = "timestamp", required = true) timeStamp: String) = {

        val result = new DeferredResult(new ModelAndView("error"))

        log.debug("Attempting to locate Shopify User Service: {} ", shop)
        mp(RegisterShopifyPartner(shop, signature, t, timeStamp)).onSuccess {
            case RegisterShopifyPartnerResponse(_, Right(envelope)) =>
                val modelAndView = new ModelAndView(v.shopifyIntegrationView)
                modelAndView.addObject("shopifyPartner", envelope.shopifyPartner)
                modelAndView.addObject("partner", envelope.partner)
                modelAndView.addObject("partnerUser", envelope.partnerUser)
                result.set(modelAndView)
            case RegisterShopifyPartnerResponse(_, Left(e: PartnerAlreadyExists)) =>
                result.set(new ModelAndView(v.partnerLoginView))
        }

        result
    }

}
