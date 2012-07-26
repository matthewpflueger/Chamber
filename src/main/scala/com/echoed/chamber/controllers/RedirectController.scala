package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import reflect.BeanProperty
import com.echoed.chamber.services.partner.{GetPartnerResponse, LocateResponse, PartnerServiceManager}

@Controller
@RequestMapping(Array("/redirect"))
class RedirectController {

    private final val logger = LoggerFactory.getLogger(classOf[RedirectController])

    @BeanProperty var errorView: String = _
    @BeanProperty var partnerServiceManager: PartnerServiceManager = _

    @RequestMapping(value = Array("partner/{partnerId}"), method = Array(RequestMethod.GET))
    def redirectPartner(
        @PathVariable(value = "partnerId") partnerId: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        partnerServiceManager.locatePartnerService(partnerId).onSuccess {
            case LocateResponse(_, Right(ps)) =>
                ps.getPartner.onSuccess {
                    case GetPartnerResponse(_, Right(p)) =>
                        logger.debug("Redirecting to {}", p.domain)
                        val modelAndView = new ModelAndView("redirect:http://" + p.domain)
                        result.set(modelAndView)
                }
        }
        result
    }



}
