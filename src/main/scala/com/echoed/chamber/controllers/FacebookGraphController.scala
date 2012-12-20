package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.feed.{GetStory, GetStoryResponse}
import com.echoed.chamber.services.partner.{GetEchoResponse, GetEcho}
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
@RequestMapping(Array("/graph"))
class FacebookGraphController extends EchoedController {

    @BeanProperty var facebookClientId: String = _
    @BeanProperty var facebookAppNameSpace: String = _

    @RequestMapping(value = Array("/story/{storyId}"), method = Array(RequestMethod.GET))
    def story(
            @PathVariable(value = "storyId") storyId: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {
        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.errorView))

        log.debug("Retrieving Story Graph Story Page for Echo: {}", storyId)
        mp(GetStory(storyId, origin)).onSuccess {
            case GetStoryResponse(msg, Right(storyFull)) =>
                log.debug("Successfully Retrived Story {} , Responding With Facebook Graph Story View", storyFull)
                val modelAndView = new ModelAndView(v.facebookGraphStoryView)
                modelAndView.addObject("storyFull", storyFull.get)
                modelAndView.addObject("facebookClientId", facebookClientId)
                modelAndView.addObject("facebookAppNameSpace", facebookAppNameSpace)
                result.setResult(modelAndView)
        }

        result
    }

    @RequestMapping(value = Array("/product/{linkId}"), method = Array(RequestMethod.GET))
    def product(@PathVariable(value = "linkId") linkId: String) = {
        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.errorView))

        log.debug("Retrieving Facebook Graph Product Page for Echo: {}", linkId)
        mp(GetEcho(linkId)).onSuccess {
            case GetEchoResponse(msg, Right(echoView)) =>
                log.debug("Successfully retreived Echo {} , Responding with Facebook Graph Product View", echoView)
                val modelAndView = new ModelAndView(v.facebookGraphProductView)
                modelAndView.addObject("echo", echoView.echo)
                modelAndView.addObject("facebookClientId", facebookClientId)
                modelAndView.addObject("facebookAppNameSpace", facebookAppNameSpace)
                result.setResult(modelAndView)
        }

        result
    }
}
