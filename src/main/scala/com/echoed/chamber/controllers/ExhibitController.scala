package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import com.echoed.chamber.services.echoeduser._
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import javax.annotation.Nullable
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.collection.JavaConversions._


@Controller
@RequestMapping(Array("/"))
class ExhibitController extends EchoedController {

    @RequestMapping(method = Array(RequestMethod.GET))
    def exhibit(
            @RequestParam(value="app", required = false) appType: String,
            @Nullable eucc: EchoedUserClientCredentials) = {

        log.debug("In exhibit with {}", eucc)

        val modelAndView = new ModelAndView(v.closetView)

        Option(eucc).map { eucc =>
            val result = new DeferredResult(modelAndView)

            mp(GetExhibit(eucc, 0, Option(appType))).onSuccess {
                case GetExhibitResponse(_, Right(closet)) =>
                    //TODO ask Jon about closet echoes not actually being set (should be a different message then)
                    modelAndView.addObject("echoedUser", closet.echoedUser)
                    modelAndView.addObject("totalCredit", "%.2f\n".format(closet.totalCredit))
                    result.set(modelAndView)
            }

            result
        }.getOrElse(modelAndView)
    }

    @RequestMapping(method = Array(RequestMethod.GET), value = Array("/{partnerHandle}"))
    def partnerExhibit(
            @PathVariable(value = "partnerHandle") partnerHandle: String,
            request: HttpServletRequest,
            response: HttpServletResponse) = {
        log.debug("Redirecting to #partner/{}", partnerHandle)
        new ModelAndView(
                v.echoRedirectView,
                Map("echo" -> Map("landingPageUrl" -> ("%s/#partner/%s" format(v.siteUrl, partnerHandle)))))
    }


}
