package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.adminuser._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.interceptors.Secure
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}


@Controller
@Secure(redirect = true, redirectToPath = "/admin/login")
class AdminDashboardController extends EchoedController {

    @RequestMapping(Array("/admin/dashboard"))
    def dashboard(aucc: AdminUserClientCredentials) = {
        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.adminDashboardErrorView))

        mp(GetAdminUser(aucc)).onSuccess {
            case GetAdminUserResponse(_, Right(au)) =>
                log.debug("Got {}", au)
                result.setResult(new ModelAndView(v.adminDashboardView, "adminUser", au))
        }

        result
    }

    @RequestMapping(Array("/admin/become"))
    def become(
            @RequestParam(value = "partnerUserId", required = true) partnerUserId: String,
            request: HttpServletRequest,
            response: HttpServletResponse,
            aucc: AdminUserClientCredentials) = {
        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.adminDashboardErrorView))

        mp(BecomePartnerUser(aucc, partnerUserId)).onSuccess {
            case BecomePartnerUserResponse(_, Right(pu)) =>
                cookieManager.addPartnerUserCookie(response, pu, request)
                result.setResult(new ModelAndView(v.partnerLoginView))
        }

        result
    }
}
