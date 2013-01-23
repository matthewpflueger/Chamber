package com.echoed.chamber.controllers.api.user

import org.springframework.stereotype.Controller
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.async.DeferredResult
import scala.util.control.Exception._
import java.lang.{NumberFormatException => NFE}
import javax.annotation.Nullable
import com.echoed.chamber.services.echoeduser._
import scala.Right
import javax.servlet.http.HttpServletResponse
import com.echoed.chamber.domain._
import scala.concurrent.ExecutionContext.Implicits.global
import views.Feed
import com.echoed.chamber.domain.views.context._
import com.echoed.chamber.services.feed._
import com.echoed.chamber.services.partner.GetTopicsResponse
import com.echoed.chamber.services.partner.GetTopics
import com.echoed.chamber.services.topic.ReadTopicFeed
import com.echoed.chamber.domain.Vote
import com.echoed.chamber.services.partner.PartnerClientCredentials
import com.echoed.chamber.services.topic.ReadTopics
import com.echoed.chamber.services.echoeduser.VoteStoryResponse
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.services.topic.ReadCommunityTopicsResponse
import com.echoed.chamber.domain.Topic
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.topic.ReadCommunityTopics
import com.echoed.chamber.services.echoeduser.VoteStory
import com.echoed.chamber.services.topic.ReadTopicFeedResponse
import com.echoed.chamber.services.topic.ReadTopicsResponse
import com.echoed.chamber.services.echoeduser.PublishFacebookAction
import com.echoed.chamber.services.topic.ReadTopicsResponse
import com.echoed.chamber.domain.Topic


@Controller
@RequestMapping(Array("/api"))
class PublicController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value = Array("/public/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def publicFeed(@RequestParam(value = "page", required = false) page: String) = {
        val result = new DeferredResult[Feed[PublicContext]](null, ErrorResult.timeout)

        mp(RequestPublicContent(parse(page))).onSuccess {
            case RequestPublicContentResponse(_, Right(feed)) => result.setResult(feed)
        }
        result
    }


}