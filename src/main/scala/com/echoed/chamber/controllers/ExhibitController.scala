package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import com.echoed.chamber.services.echoeduser._
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import javax.annotation.{Resource, Nullable}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.collection.JavaConversions._
import java.util.{List => JList}


@Controller
@RequestMapping(Array("/"))
class ExhibitController extends EchoedController {

    @Resource(name = "mobileUserAgents") var mobileUserAgents: JList[String] = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def exhibit(
            @RequestParam(value="app", required = false) appType: String,
            @Nullable eucc: EchoedUserClientCredentials,
            @RequestHeader("User-Agent") userAgent: String,
            request: HttpServletRequest) = {

        Option(request.getAttribute("isSecure"))
            .filter(_ == true)
            .map(_ => new ModelAndView("redirect:%s" format v.siteUrl))
            .getOrElse {
                log.debug("User Agent: {}", userAgent)

                val modelAndView = if(mobileUserAgents.exists(userAgent.contains(_))){
                    new ModelAndView(v.mobileUserView)
                } else {
                    new ModelAndView(v.closetView)
                }

                Option(eucc).map(modelAndView.addObject("echoedUser", _)).getOrElse(modelAndView)
            }
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
