package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.partner._
import com.echoed.chamber.services.echoeduser.GetEchoedUser
import com.echoed.chamber.services.echoeduser.GetEchoedUserResponse
import com.echoed.chamber.services.partner.FetchPartnerResponse
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.partner.FetchPartner
import scala.Right
import scala.concurrent.ExecutionContext.Implicits.global


@Controller
@RequestMapping(Array("/app"))
class AppController extends EchoedController {

    @RequestMapping(value = Array("/{pid}"), method = Array(RequestMethod.GET))
    def app(
            @PathVariable(value = "pid") pid: String,
            @RequestParam(value = "cid", required = false) id: String) = {

        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.errorView))

        mp(FetchPartner(new PartnerClientCredentials(pid))).onSuccess {
            case FetchPartnerResponse(_, Right(p)) =>
                val domain = if (p.domain.startsWith("http")) p.domain else "http://" + p.domain
                val modelAndView = new ModelAndView(v.appView)
                modelAndView.addObject("contentId", id)
                modelAndView.addObject("domain", domain)
                modelAndView.addObject("partner", p)
                result.setResult(modelAndView)
        }
        result
    }

    @RequestMapping(value = Array("/iframe"), method = Array(RequestMethod.GET))
    def appIframe = v.appIFrameView
}
