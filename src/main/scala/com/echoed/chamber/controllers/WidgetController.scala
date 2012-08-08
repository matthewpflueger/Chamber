package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import com.echoed.chamber.services.partner._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.event.EventService


@Controller
@RequestMapping(Array("/widget"))
class WidgetController {

    private final val logger = LoggerFactory.getLogger(classOf[WidgetController])

    @BeanProperty var partnerServiceManager: PartnerServiceManager = _
    @BeanProperty var widgetJsView: String = _
    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var eventService: EventService = _

    @RequestMapping(value = Array("/iframe"), method = Array(RequestMethod.GET))
    def iframe(
        @RequestParam(value = "pid", required = true) pid: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView("error"))
        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse("")
        partnerServiceManager.locatePartnerService(pid).onSuccess {
            case LocateResponse(_, Right(partnerService)) =>
                val modelAndView = new ModelAndView("widget.iframe")
                modelAndView.addObject("partnerId", pid)
                modelAndView.addObject("echoedUserId", echoedUserId)
                result.set(modelAndView)
        }

        result
    }

    @RequestMapping(value = Array("/js"), method = Array(RequestMethod.GET), produces = Array("application/x-javascript"))
    def js(
        @RequestParam(value = "pid", required = true) pid: String,
        @RequestParam(value = "style", required = false, defaultValue = "black") style: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView("error"))
        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse("")

        logger.debug("Retrieving Javascript Widget for PartnerId: {}", pid)
        partnerServiceManager.locatePartnerService(pid).onSuccess {
            case LocateResponse(_, Right(partnerService)) =>
                val modelAndView = new ModelAndView(widgetJsView)
                modelAndView.addObject("partnerId", pid)
                modelAndView.addObject("echoedUserId", echoedUserId)
                if(style.equals("white")){
                    modelAndView.addObject("white", true)
                } else {
                    modelAndView.addObject("black", true)
                }
                eventService.widgetRequested(pid)
                result.set(modelAndView)
        }

        result

    }

}
