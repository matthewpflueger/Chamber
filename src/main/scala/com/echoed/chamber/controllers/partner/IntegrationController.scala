package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller

import scala.reflect.BeanProperty

import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import com.echoed.chamber.controllers.CookieManager
import com.echoed.chamber.services.partneruser.{GetPartnerUserResponse, LocateResponse, PartnerUserServiceLocator}
import org.springframework.web.context.request.async.DeferredResult


@Controller
class IntegrationController {

    private val logger = LoggerFactory.getLogger(classOf[IntegrationController])

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var integrationView: String = _
    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @RequestMapping(value = Array("/partner/integration"), method = Array(RequestMethod.GET))
    def integration(request: HttpServletRequest, response: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(integrationView))

        val partnerUserId = cookieManager.findPartnerUserCookie(request)

        logger.debug("Showing integration pages for PartnerUser {}", partnerUserId)

        partnerUserServiceLocator.locate(partnerUserId.get).onSuccess {
            case LocateResponse(_, Right(partnerUserService)) => partnerUserService.getPartnerUser.onSuccess {
                case GetPartnerUserResponse(_, Right(partnerUser)) =>
                    result.set(new ModelAndView(integrationView, "partnerUser", partnerUser))
            }
        }

        result
    }

}
