package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.partner.{GetPartner, GetPartnerResponse}
import com.echoed.chamber.services.echoeduser._
import com.echoed.chamber.services.echoeduser.GetEchoedUser
import com.echoed.chamber.services.echoeduser.GetEchoedUserResponse
import scala.Right

@Controller
@RequestMapping(Array("/redirect"))
class RedirectController extends EchoedController {

    @RequestMapping(value = Array("partner/{partnerId}"), method = Array(RequestMethod.GET))
    def redirectPartner(@PathVariable(value = "partnerId") partnerId: String) = {
        val result = new DeferredResult(new ModelAndView(v.errorView))

        mp(GetPartner()).onSuccess {
            case GetPartnerResponse(_, Right(p)) =>
                log.debug("Redirecting to {}", p.domain)
                val domain = if (p.domain.startsWith("http")) p.domain else "http://" + p.domain
                val modelAndView = new ModelAndView("redirect:" + domain)
                result.set(modelAndView)
        }

        result
    }

    @RequestMapping(value = Array("/close"), method = Array(RequestMethod.GET))
    def close(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult(new ModelAndView(v.errorView))

        mp(GetEchoedUser(eucc)).onSuccess {
            case GetEchoedUserResponse(_, Right(echoedUser)) =>
                val modelAndView = new ModelAndView(v.echoAuthComplete)
                modelAndView.addObject("echoedUserName", echoedUser.name)
                modelAndView.addObject("facebookUserId", echoedUser.facebookId)
                modelAndView.addObject("twitterUserId", echoedUser.twitterId)
                result.set(modelAndView)
        }

        result
    }




}
