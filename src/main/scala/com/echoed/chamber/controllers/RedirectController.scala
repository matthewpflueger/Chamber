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


@Controller
@RequestMapping(Array("/redirect"))
class RedirectController extends EchoedController {

    @RequestMapping(value = Array("partner/{pid}"), method = Array(RequestMethod.GET))
    def redirectPartner(
        @RequestParam(value = "id", required = false) storyId: String,
        pcc: PartnerClientCredentials) = {

        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.errorView))

        mp(FetchPartner(pcc)).onSuccess {
            case FetchPartnerResponse(_, Right(p)) =>
                log.debug("Redirecting to {}", p.domain)
                val domain = if (p.domain.startsWith("http")) p.domain else "http://" + p.domain
                val modelAndView = new ModelAndView(v.redirectView)

                var redirectUrl = if (p.isEchoed) v.siteUrl + "/#!" else domain + "/#echoed_"

                if(storyId != null) redirectUrl += "story/" + storyId

                modelAndView.addObject("redirectUrl" , redirectUrl)
                result.setResult(modelAndView)
        }

        result
    }

    @RequestMapping(value = Array("/close"), method = Array(RequestMethod.GET))
    def close(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.errorView))

        mp(GetEchoedUser(eucc)).onSuccess {
            case GetEchoedUserResponse(_, Right(echoedUser)) =>
                val modelAndView = new ModelAndView(v.echoAuthComplete)
                modelAndView.addObject("echoedUserName", echoedUser.name)
                modelAndView.addObject("echoedUserId", echoedUser.id)
                modelAndView.addObject("facebookUserId", echoedUser.facebookId)
                modelAndView.addObject("twitterUserId", echoedUser.twitterId)
                result.setResult(modelAndView)
        }

        result
    }




}
