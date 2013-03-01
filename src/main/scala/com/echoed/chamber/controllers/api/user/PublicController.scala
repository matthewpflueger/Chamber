package com.echoed.chamber.controllers.api.user

import org.springframework.stereotype.Controller
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.async.DeferredResult
import scala.util.control.Exception._
import java.lang.{NumberFormatException => NFE}
import scala.Right
import com.echoed.chamber.domain._
import scala.concurrent.ExecutionContext.Implicits.global
import views.content.{PhotoContent, ContentDescription}
import views.Feed
import com.echoed.chamber.domain.views.context._
import com.echoed.chamber.services.feed._


@Controller
@RequestMapping(Array("/api"))
class PublicController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value = Array("/public/feed", "/public/feed/stories"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPublicStories(@RequestParam(value = "page", required = false) page: String) = getPublicContent(Story.storyContentDescription, page)

    @RequestMapping(value = Array("/public/feed/reviews"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPublicReviews(@RequestParam(value = "page", required = false) page: String) = getPublicContent(Story.reviewContentDescription, page)

    @RequestMapping(value = Array("/public/feed/photos"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPublicPhotos(@RequestParam(value = "page", required = false) page: String) = getPublicContent(PhotoContent.contentDescription, page)


    def getPublicContent(contentType: ContentDescription,
                         page: String) = {

        val result = new DeferredResult[Feed[PublicContext]](null, ErrorResult.timeout)

        mp(RequestPublicContent(contentType, parse(page))).onSuccess {
            case RequestPublicContentResponse(_, Right(feed)) => result.setResult(feed)
        }
        result

    }



}