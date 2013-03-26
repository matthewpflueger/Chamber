package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import com.echoed.chamber.services.echoeduser._
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.annotation.{Resource, Nullable}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.collection.JavaConversions._
import java.util.{List => JList}
import reflect.BeanProperty


@Controller
@RequestMapping(Array("/"))
class ExhibitController extends EchoedController {

    @Resource(name = "mobileUserAgents") var mobileUserAgents: JList[String] = _
    @BeanProperty var bookmarkletName: String = _

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

                Option(eucc).map({
                    ec: EchoedUserClientCredentials =>
                        val modelAndView = new ModelAndView(v.appView)
                        modelAndView.addObject("echoedUser", ec)
                        modelAndView
                }).getOrElse {
                    val modelAndView = new ModelAndView(v.whatIsEchoedView)
                    modelAndView.addObject("bookmarkletName", bookmarkletName)
                    if(userAgent.contains("Chrome")){
                        modelAndView.addObject("isChrome", true)
                    } else if(userAgent.contains("Safari")){
                        modelAndView.addObject("isSafari", true)
                    } else if(userAgent.contains("MSIE")){
                        modelAndView.addObject("isIE", true)
                    } else if(userAgent.contains("Firefox")){
                        modelAndView.addObject("isFirefox", true)
                    }
                    modelAndView
                }
            }
    }

    @RequestMapping(method = Array(RequestMethod.GET), value = Array("/explore"))
    def app(
            @Nullable eucc: EchoedUserClientCredentials,
            request: HttpServletRequest) = {

        Option(eucc)
            .map(_ => new ModelAndView("redirect:%s" format v.siteUrl))
            .getOrElse(new ModelAndView(v.appView))

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
