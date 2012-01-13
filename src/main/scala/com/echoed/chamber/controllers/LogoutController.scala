package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eclipse.jetty.continuation.ContinuationSupport
import org.springframework.web.bind.annotation._
import reflect.BeanProperty
import com.echoed.util.CookieManager
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping(Array("/logout"))
class LogoutController {

    private val logger = LoggerFactory.getLogger(classOf[LogoutController])

    @BeanProperty var cookieManager: CookieManager = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def logout(
               @RequestParam(value = "redirect", required=false) redirect: String,
               httpServletResponse: HttpServletResponse,
               httpServletRequest: HttpServletRequest) = {
        logger.debug("Removing cookies: echoedUserId & partnerUser");
        cookieManager.deleteCookie(httpServletResponse, "echoedUserId");
        cookieManager.deleteCookie(httpServletResponse, "partnerUser");
        if(redirect != null)
            new ModelAndView("redirect:http://v1-api.echoed.com/" + redirect);
        else
            new ModelAndView("redirect:http://www.echoed.com/")

    }

}
