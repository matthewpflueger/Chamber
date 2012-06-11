package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod,ResponseBody}
import com.echoed.chamber.services.feed._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.domain.views.PublicFeed


@Controller
@RequestMapping(Array("/public"))
class PublicController {

    private val logger = LoggerFactory.getLogger(classOf[PublicController])

    @BeanProperty var feedService: FeedService = _


    @RequestMapping(value = Array("/feed"), method = Array(RequestMethod.GET), produces = Array("application/json"))
    @ResponseBody
    def getPublicFeedJSON(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse): DeferredResult = {

        val result = new DeferredResult(new PublicFeed())

        feedService.getPublicFeed.onSuccess {
            case GetPublicFeedResponse(_, Right(feed)) =>
                logger.debug("Found public feed of size {}", feed.echoes.size())
                result.set(feed)
        }

        result
    }
}
