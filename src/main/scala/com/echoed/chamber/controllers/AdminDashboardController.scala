package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.adminuser._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.interceptors.Secure


@Controller
@Secure(redirect = true, redirectToPath = "/admin/login")
class AdminDashboardController extends EchoedController {

    @RequestMapping(Array("/admin/dashboard"))
    def dashboard(aucc: AdminUserClientCredentials) = {
        val result = new DeferredResult(new ModelAndView(v.adminDashboardErrorView))

        mp(GetAdminUser(aucc)).onSuccess {
            case GetAdminUserResponse(_, Right(au)) =>
                log.debug("Got {}", au)
                result.set(new ModelAndView(v.adminDashboardView, "adminUser", au))
        }

        result
    }
}
