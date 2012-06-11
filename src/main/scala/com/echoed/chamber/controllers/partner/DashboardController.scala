package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import com.echoed.chamber.controllers.CookieManager
import org.springframework.web.context.request.async.DeferredResult


@Controller
class DashboardController {

    private val logger = LoggerFactory.getLogger(classOf[DashboardController])

    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @BeanProperty var partnerDashboardErrorView: String = _
    @BeanProperty var partnerDashboardView: String = _
    @BeanProperty var cookieManager: CookieManager = _


    @RequestMapping(value = Array("/partner/dashboard"), method = Array(RequestMethod.GET))
    def dashboard(
             httpServletRequest: HttpServletRequest,
             httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(partnerDashboardErrorView))
        val partnerUserId = cookieManager.findPartnerUserCookie(httpServletRequest)

        logger.debug("Showing dashboard for PartnerUser {}", partnerUserId)
        partnerUserServiceLocator.locate(partnerUserId.get).onSuccess {
            case LocateResponse(_, Right(pus)) => pus.getPartnerUser.onSuccess {
                case GetPartnerUserResponse(_, Right(pu)) =>
                    logger.debug("Got {}", pu)
                    result.set(new ModelAndView(partnerDashboardView, "partnerUser", pu))
            }
        }

        result
    }

}
