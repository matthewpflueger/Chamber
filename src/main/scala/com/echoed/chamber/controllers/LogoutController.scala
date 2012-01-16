package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import javax.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation._
import reflect.BeanProperty
import com.echoed.util.CookieManager
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.partneruser.PartnerUserServiceLocator



@Controller
@RequestMapping(Array("/logout"))
class LogoutController {

    private val logger = LoggerFactory.getLogger(classOf[LogoutController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def logout(
            @RequestParam(value = "redirect", required=false) redirect: String,
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            @CookieValue(value = "partnerUser", required = false) partnerUser: String,
            httpServletResponse: HttpServletResponse) = {


        logger.debug("Removing cookies: echoedUserId {} and partnerUser {}", echoedUserId, partnerUser);
        cookieManager.deleteCookie(httpServletResponse, "echoedUserId");
        cookieManager.deleteCookie(httpServletResponse, "partnerUser");
        Option(echoedUserId).foreach(echoedUserServiceLocator.logout(_))
        Option(partnerUser).foreach(partnerUserServiceLocator.logout(_))

        if (redirect != null)
            new ModelAndView("redirect:http://v1-api.echoed.com/" + redirect);
        else
            new ModelAndView("redirect:http://www.echoed.com/")

    }

}
