package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.EchoedController


@Controller
class DashboardController extends EchoedController {

    @RequestMapping(value = Array("/partner/dashboard"), method = Array(RequestMethod.GET))
    def dashboard(pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult(new ModelAndView(v.partnerDashboardErrorView))

        log.debug("Showing dashboard for {}", pucc)
        mp(GetPartnerUser(pucc)).onSuccess {
            case GetPartnerUserResponse(_, Right(pu)) =>
                log.debug("Got {}", pu)
                result.set(new ModelAndView(v.partnerDashboardView, "partnerUser", pu))
        }

        result
    }

}
