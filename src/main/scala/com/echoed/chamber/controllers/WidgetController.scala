package com.echoed.chamber.controllers

import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.partner._
import javax.annotation.Nullable
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.web.servlet.ModelAndView
import scala.Right
import scala.concurrent.ExecutionContext.Implicits.global


@Controller
@RequestMapping(Array("/widget"))
class WidgetController extends EchoedController {

    @RequestMapping(value = Array("/iframe"), method = Array(RequestMethod.GET))
    def iframe(
            pcc: PartnerClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.errorView))

        mp(FetchPartnerAndPartnerSettings(pcc)).onSuccess {
            case FetchPartnerAndPartnerSettingsResponse(_, Right(p)) =>
                val modelAndView = new ModelAndView(v.widgetAppIFrameView)
                modelAndView.addObject("partner", p.partner)
                modelAndView.addObject("customization", p.customization)
                modelAndView.addObject("partnerId", pcc.partnerId)
                modelAndView.addObject("echoedUser", eucc)
                result.setResult(modelAndView)
        }
        //FIXME should be requesting the widget view from the service which then publishes the event
        result
    }

    @RequestMapping(value = Array("/iframe/gallery"), method = Array(RequestMethod.GET))
    def iframeGallery(
            pcc: PartnerClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.errorView))

        mp(FetchPartnerAndPartnerSettings(pcc)).onSuccess {
            case FetchPartnerAndPartnerSettingsResponse(_, Right(p)) =>
                val modelAndView = new ModelAndView(v.widgetAppIFrameGalleryView)
                modelAndView.addObject("customization", p.customization)
                modelAndView.addObject("partner", p.partner)
                modelAndView.addObject("partnerId", pcc.partnerId)
                result.setResult(modelAndView)
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
            pcc: PartnerClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials,
            @RequestHeader("User-Agent") userAgent: String) = {

        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.errorView))
        if(!userAgent.contains("MSIE 8")){
            mp(FetchPartnerAndPartnerSettings(pcc)).onSuccess {
                case FetchPartnerAndPartnerSettingsResponse(_, Right(p)) =>
                    val modelAndView =  new ModelAndView(v.widgetAppJsView)
                    modelAndView.addObject("partnerId", pcc.partnerId)
                    modelAndView.addObject("partner", p.partner)
                    modelAndView.addObject("echoedUserId", Option(eucc).map(_.id).getOrElse(""))
                    modelAndView.addObject("customization", p.customization)
                    result.setResult(modelAndView)
            }
        }
        result

    }

}
