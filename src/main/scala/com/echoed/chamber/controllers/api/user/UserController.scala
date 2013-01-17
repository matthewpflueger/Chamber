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
import views.ContentFeed
import com.echoed.chamber.domain.views.context._
import com.echoed.chamber.services.echoeduser.{RequestUserContentFeedResponse, RequestUserContentFeed, EchoedUserClientCredentials}


@Controller
@RequestMapping(Array("/api/user"))
class UserController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value= Array("/{id}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def userFeed(
                    @PathVariable(value ="id") id: String,
                    @RequestParam(value = "page", required = false) page: String,
                    @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        log.debug("Getting feed for {}", id)

        val result = new DeferredResult[ContentFeed[UserContext]](null, ErrorResult.timeout)

        mp(RequestUserContentFeed(new EchoedUserClientCredentials(id), parse(page), "story")).onSuccess {
            case RequestUserContentFeedResponse(_, Right(feed)) => result.setResult(feed)
        }

        result
    }

    @RequestMapping(value= Array("/{id}/photos"), method=Array(RequestMethod.GET))
    @ResponseBody
    def userPhotoFeed(
                         @PathVariable(value ="id") id: String,
                         @RequestParam(value = "page", required = false) page: String,
                         @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        log.debug("Getting feed for {}", id)

        val result = new DeferredResult[ContentFeed[UserContext]](null, ErrorResult.timeout)

        mp(RequestUserContentFeed(new EchoedUserClientCredentials(id), parse(page), "photo")).onSuccess {
            case RequestUserContentFeedResponse(_, Right(feed)) => result.setResult(feed)
        }

        result
    }

}