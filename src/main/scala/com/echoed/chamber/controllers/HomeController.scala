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
@RequestMapping(Array("/"))
class HomeController {

    private val logger = LoggerFactory.getLogger(classOf[HomeController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def logout(
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            httpServletResponse: HttpServletResponse) = {

        if (echoedUserId != null) {
            logger.debug("Redirecting to closet")
            new ModelAndView("redirect:http://v1-api.echoed.com/closet")
        } else {
            logger.debug("Serving index")
            new ModelAndView("view.index")
        }

    }

}
