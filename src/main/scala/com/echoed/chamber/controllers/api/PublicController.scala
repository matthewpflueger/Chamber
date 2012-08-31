package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestParam, RequestMapping, RequestMethod, ResponseBody}
import com.echoed.chamber.services.feed._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.domain.views.PublicFeed
import com.echoed.chamber.controllers.EchoedController


@Controller
@RequestMapping(Array("/public"))
class PublicController extends EchoedController {

    @RequestMapping(value = Array("/feed"), method = Array(RequestMethod.GET), produces = Array("application/json"))
    @ResponseBody
    def getPublicFeedJSON(@RequestParam(value = "page", required = false, defaultValue = "0") page: Int) = {
        val result = new DeferredResult(new PublicFeed())

        mp(GetPublicFeed(page)).onSuccess {
            case GetPublicFeedResponse(_, Right(feed)) =>
                log.debug("Found public feed of size {}", feed.echoes.size())
                result.set(feed)
        }

        result
    }
}
