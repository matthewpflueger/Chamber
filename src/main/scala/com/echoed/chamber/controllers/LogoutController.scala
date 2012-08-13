package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.adminuser
import com.echoed.chamber.services.echoeduser
import com.echoed.chamber.services.partneruser
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials
import javax.annotation.Nullable


@Controller
@RequestMapping(Array("/logout"))
class LogoutController extends EchoedController {

    @RequestMapping(method = Array(RequestMethod.GET))
    def logout(
            @RequestParam(value = "redirect", required = false) redirect: String,
            @Nullable aucc: AdminUserClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials,
            @Nullable pucc: PartnerUserClientCredentials,
            request: HttpServletRequest,
            response: HttpServletResponse) = {

        Option(aucc).foreach(c => mp(adminuser.Logout(c)))
        Option(eucc).foreach(c => mp(echoeduser.Logout(c)))
        Option(pucc).foreach(c => mp(partneruser.Logout(c)))

        cookieManager.addEchoedUserCookie(response, request = request)
        cookieManager.addPartnerUserCookie(response, request = request)
        cookieManager.addAdminUserCookie(response, request = request)

        new ModelAndView("%s/%s" format (v.postLogoutView, Option(redirect).getOrElse("")))
    }

}
