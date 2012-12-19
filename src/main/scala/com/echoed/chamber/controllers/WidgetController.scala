package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.event.WidgetRequested
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import javax.annotation.Nullable
import com.echoed.chamber.services.partner._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.partner.FetchPartnerAndPartnerSettings
import com.echoed.chamber.services.event.WidgetRequested
import com.echoed.chamber.services.partner.FetchPartnerResponse
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.partner.FetchPartner
import com.echoed.chamber.services.partner.PartnerClientCredentials
import scala.Right


@Controller
@RequestMapping(Array("/widget"))
class WidgetController extends EchoedController {

    @RequestMapping(value = Array("/iframe"), method = Array(RequestMethod.GET))
    def iframe(
            pcc: PartnerClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult(new ModelAndView(v.errorView))

        mp(FetchPartnerAndPartnerSettings(pcc)).onSuccess {
            case FetchPartnerAndPartnerSettingsResponse(_, Right(p)) =>
                val modelAndView = new ModelAndView(v.widgetAppIFrameView)
                modelAndView.addObject("partner", p.partner)
                modelAndView.addObject("customization", p.customization)
                modelAndView.addObject("partnerId", pcc.partnerId)
                modelAndView.addObject("echoedUserId", Option(eucc).map(_.id).getOrElse(""))
                result.set(modelAndView)
        }
        //FIXME should be requesting the widget view from the service which then publishes the event
        result
    }

    @RequestMapping(value = Array("/iframe/gallery"), method = Array(RequestMethod.GET))
    def iframeGallery(
            pcc: PartnerClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult(new ModelAndView(v.errorView))

        mp(FetchPartnerAndPartnerSettings(pcc)).onSuccess {
            case FetchPartnerAndPartnerSettingsResponse(_, Right(p)) =>
                val modelAndView = new ModelAndView(v.widgetAppIFrameGalleryView)
                modelAndView.addObject("customization", p.customization)
                modelAndView.addObject("partner", p.partner)
                modelAndView.addObject("partnerId", pcc.partnerId)
                result.set(modelAndView)
        }

        result
    }

    @RequestMapping(value = Array("/iframe/preview"), method = Array(RequestMethod.GET))
    def iframePreview(
            pcc: PartnerClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials) = {

        //val result = new DeferredResult(new ModelAndView(v.errorView))
        val modelAndView = new ModelAndView(v.widgetAppIFramePreviewView)
        modelAndView
    }

    @RequestMapping(
            value = Array("/js"),
            method = Array(RequestMethod.GET),
            produces = Array("application/x-javascript"))
    def js(
            @RequestParam(value = "style", required = false, defaultValue = "black") style: String,
            @RequestParam(value = "side", required = false, defaultValue = "left") side: String,
            pcc: PartnerClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult(new ModelAndView(v.errorView))

        mp(FetchPartnerAndPartnerSettings(pcc)).onSuccess {
            case FetchPartnerAndPartnerSettingsResponse(_, Right(p)) =>
                val modelAndView =  new ModelAndView(v.widgetAppJsView)
                modelAndView.addObject("partnerId", pcc.partnerId)
                modelAndView.addObject("partner", p.partner)
                modelAndView.addObject("echoedUserId", Option(eucc).map(_.id).getOrElse(""))
                modelAndView.addObject("customization", p.customization)
                modelAndView.addObject("side", side)
                if(style.equals("white")){
                    modelAndView.addObject("white", true)
                } else {
                    modelAndView.addObject("black", true)
                }
                //ep.publish(WidgetRequested(pcc.partnerId))
                result.set(modelAndView)


        }

        result

    }

}
