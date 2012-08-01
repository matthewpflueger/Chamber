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
import com.echoed.chamber.services.echoeduser.{GetEchoedUserResponse, LocateWithIdResponse, EchoedUserServiceLocator}

@Controller
@RequestMapping(Array("/redirect"))
class RedirectController {

    private final val logger = LoggerFactory.getLogger(classOf[RedirectController])

    @BeanProperty var errorView: String = _
    @BeanProperty var partnerServiceManager: PartnerServiceManager = _
    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoAuthComplete: String = _

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
                        val domain = if (p.domain.startsWith("http")) p.domain else "http://" + p.domain
                        val modelAndView = new ModelAndView("redirect:" + domain)
                        result.set(modelAndView)
                }
        }
        result
    }

    @RequestMapping(value = Array("/close"), method = Array(RequestMethod.GET))
    def close(
                 httpServletRequest: HttpServletRequest,
                 httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        val eu= cookieManager.findEchoedUserCookie(httpServletRequest).get

        echoedUserServiceLocator.getEchoedUserServiceWithId(eu).onSuccess {
            case LocateWithIdResponse(_, Right(eus)) =>
                eus.getEchoedUser.onSuccess {
                    case GetEchoedUserResponse(_, Right(echoedUser)) =>
                        val modelAndView = new ModelAndView(echoAuthComplete)
                        modelAndView.addObject("echoedUserName", echoedUser.name)
                        modelAndView.addObject("facebookUserId", echoedUser.facebookId)
                        modelAndView.addObject("twitterUserId", echoedUser.twitterId)
                        result.set(modelAndView)
                }
        }
        result
    }




}
