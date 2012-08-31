package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.event.WidgetRequested
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import javax.annotation.Nullable
import com.echoed.chamber.services.partner.{FetchPartnerResponse, FetchPartner, PartnerClientCredentials}
import org.springframework.web.context.request.async.DeferredResult


@Controller
@RequestMapping(Array("/widget"))
class WidgetController extends EchoedController {

    @RequestMapping(value = Array("/iframe"), method = Array(RequestMethod.GET))
    def iframe(
            pcc: PartnerClientCredentials,
            @RequestParam(value = "type", required = false, defaultValue = "app") widgetType: String,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult(new ModelAndView(v.errorView))

        mp(FetchPartner(pcc)).onSuccess {
            case FetchPartnerResponse(_, Right(partner)) =>
                val modelAndView = if(widgetType == "widget") new ModelAndView(v.widgetIframeView) else new ModelAndView(v.widgetAppIFrameView)
                modelAndView.addObject("partner", partner)
                modelAndView.addObject("partnerId", pcc.partnerId)
                modelAndView.addObject("echoedUserId", Option(eucc).map(_.echoedUserId).getOrElse(""))
                result.set(modelAndView)
        }
        //FIXME should be requesting the widget view from the service which then publishes the event
        result
    }

    @RequestMapping(
            value = Array("/js"),
            method = Array(RequestMethod.GET),
            produces = Array("application/x-javascript"))
    def js(
            @RequestParam(value = "style", required = false, defaultValue = "black") style: String,
            @RequestParam(value = "type", required = false, defaultValue = "app") widgetType: String,
            pcc: PartnerClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val modelAndView = if(widgetType == "widget") new ModelAndView(v.widgetJsView) else new ModelAndView(v.widgetAppJsView)
        modelAndView.addObject("partnerId", pcc.partnerId)
        modelAndView.addObject("echoedUserId", Option(eucc).map(_.echoedUserId).getOrElse(""))

        if(style.equals("white")){
            modelAndView.addObject("white", true)
        } else {
            modelAndView.addObject("black", true)
        }
        //FIXME should be requesting the widget view from the service which then publishes the event
        ep.publish(WidgetRequested(pcc.partnerId))
        modelAndView
    }

}
