package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.springframework.web.servlet.ModelAndView
import scala.collection.JavaConversions
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod,ResponseBody,PathVariable}
import com.echoed.chamber.services.feed._

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/7/12
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(Array("/public"))
class PublicController {

    private val logger = LoggerFactory.getLogger(classOf[PublicController])

    @BeanProperty var feedService: FeedService = _
    
    @RequestMapping(value = Array("/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPublicFeedJSON(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired){
            logger.error("Error Getting Public Feed")
            "Error"
        } else Option(continuation.getAttribute("feed")).getOrElse({

            continuation.suspend(httpServletResponse)
            feedService.getPublicFeed.onResult({
                case GetPublicFeedResponse(_, Left(error)) =>
                    logger.error("Error Getting Public Feed")
                    "Error"
                case GetPublicFeedResponse(_, Right(feed)) =>
                    continuation.setAttribute("feed", feed)
                    continuation.resume()
                case unknown => throw new RuntimeException("Unknown Response %s" format unknown)

            })
            continuation.undispatch()
        })
    }
}
