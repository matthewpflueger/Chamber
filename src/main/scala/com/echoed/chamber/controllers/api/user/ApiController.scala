package com.echoed.chamber.controllers.api.user

import org.springframework.stereotype.Controller
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.async.DeferredResult
import scala.util.control.Exception._
import java.lang.{NumberFormatException => NFE}
import javax.annotation.Nullable
import scala.Right
import javax.servlet.http.HttpServletResponse
import com.echoed.chamber.domain._
import scala.concurrent.ExecutionContext.Implicits.global
import com.echoed.chamber.services.feed.GetStoryResponse
import com.echoed.chamber.services.feed.GetStory
import com.echoed.chamber.domain.Vote
import com.echoed.chamber.services.echoeduser.VoteStoryResponse
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.echoeduser.VoteStory
import com.echoed.chamber.services.echoeduser.PublishFacebookAction


@Controller
@RequestMapping(Array("/api"))
class ApiController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value = Array("/story/{id}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getStory(
            @PathVariable(value = "id") id: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[Option[StoryPublic]](null, ErrorResult.timeout)

        log.debug("Requesting Story {}", id )

        mp(GetStory(id, origin)).onSuccess {
            case GetStoryResponse(_, Right(story)) => result.setResult(story)
        }

        Option(eucc).map(c => mp(PublishFacebookAction(c, "browse", "story", v.storyGraphUrl + id)))
        result
    }

    @RequestMapping(value = Array("/upvote"), method = Array(RequestMethod.GET))
    @ResponseBody
    def upVote(
              eucc: EchoedUserClientCredentials,
              response: HttpServletResponse,
              @RequestParam(value = "storyId", required = true) storyId: String,
              @RequestParam(value = "storyOwnerId", required = true) storyOwnerId: String) = {

        val result = new DeferredResult[Map[String, Vote]](null, ErrorResult.timeout)

        mp(VoteStory(eucc, storyOwnerId, storyId, 1)).onSuccess {
            case VoteStoryResponse(_, Right(votes)) =>
                result.setResult(votes)
        }
        result
    }

    @RequestMapping(value = Array("/downvote"), method = Array(RequestMethod.GET))
    @ResponseBody
    def downVote(
                eucc: EchoedUserClientCredentials,
                response: HttpServletResponse,
                @RequestParam(value = "storyId", required = true) storyId: String,
                @RequestParam(value = "storyOwnerId", required = true) storyOwnerId: String) = {

        val result = new DeferredResult[Map[String, Vote]](null, ErrorResult.timeout)
        mp(VoteStory(eucc, storyOwnerId, storyId, -1)).onSuccess {
            case VoteStoryResponse(_, Right(votes)) =>
                result.setResult(votes)
        }
        result
    }

}