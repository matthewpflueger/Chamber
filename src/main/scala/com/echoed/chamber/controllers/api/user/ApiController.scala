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
import scala.collection.immutable.Stack
import scala.concurrent.ExecutionContext.Implicits.global
import views.CommunityFeed
import views.ContentFeed
import com.echoed.chamber.domain.views.context._
import com.echoed.chamber.services.feed._
import com.echoed.chamber.services.feed.GetStoryResponse
import com.echoed.chamber.services.echoeduser.FetchNotifications
import com.echoed.chamber.services.echoeduser.ReadSettingsResponse
import com.echoed.chamber.services.echoeduser.UnFollowPartnerResponse
import com.echoed.chamber.services.partner.GetTopicsResponse
import com.echoed.chamber.services.echoeduser.RequestUserContentFeedResponse
import com.echoed.chamber.services.echoeduser.UnFollowPartner
import com.echoed.chamber.services.echoeduser.ListFollowedByUsers
import com.echoed.chamber.services.partner.GetTopics
import com.echoed.chamber.services.echoeduser.NewSettingsResponse
import com.echoed.chamber.services.partner.RequestPartnerContentFeed
import com.echoed.chamber.services.topic.ReadTopicFeed
import com.echoed.chamber.services.feed.GetCommunities
import com.echoed.chamber.services.feed.GetStory
import com.echoed.chamber.domain.Vote
import com.echoed.chamber.services.echoeduser.MarkNotificationsAsRead
import com.echoed.chamber.domain.EchoedUserSettings
import com.echoed.chamber.services.echoeduser.RequestOwnContentResponse
import com.echoed.chamber.services.echoeduser.NewSettings
import com.echoed.chamber.services.echoeduser.ListFollowingPartnersResponse
import com.echoed.chamber.services.echoeduser.RequestUserContentFeed
import com.echoed.chamber.services.echoeduser.FollowPartnerResponse
import com.echoed.chamber.services.echoeduser.PartnerFollower
import com.echoed.chamber.services.partner.PartnerClientCredentials
import com.echoed.chamber.services.topic.ReadTopics
import com.echoed.chamber.services.echoeduser.UnFollowUserResponse
import com.echoed.chamber.services.echoeduser.FollowUserResponse
import com.echoed.chamber.services.feed.GetCommunitiesResponse
import com.echoed.chamber.services.echoeduser.VoteStoryResponse
import com.echoed.chamber.services.echoeduser.UnFollowUser
import com.echoed.chamber.services.echoeduser.RequestCustomUserFeed
import com.echoed.chamber.services.echoeduser.FollowPartner
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.services.feed.GetCategoryStoryFeedResponse
import com.echoed.chamber.services.feed.GetCategoryStoryFeed
import com.echoed.chamber.services.topic.ReadCommunityTopicsResponse
import com.echoed.chamber.services.partner.ReadPartnerFeed
import com.echoed.chamber.services.echoeduser.RequestCustomUserFeedResponse
import com.echoed.chamber.services.echoeduser.MarkNotificationsAsReadResponse
import com.echoed.chamber.services.echoeduser.ListFollowedByUsersResponse
import com.echoed.chamber.services.echoeduser.RequestOwnContent
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.domain.Topic
import com.echoed.chamber.services.echoeduser.ReadSettings
import com.echoed.chamber.services.echoeduser.FetchNotificationsResponse
import com.echoed.chamber.services.echoeduser.ListFollowingUsers
import com.echoed.chamber.services.echoeduser.ListFollowingPartners
import com.echoed.chamber.services.echoeduser.ListFollowingUsersResponse
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.topic.ReadCommunityTopics
import com.echoed.chamber.services.partner.ReadPartnerFeedResponse
import com.echoed.chamber.services.echoeduser.VoteStory
import com.echoed.chamber.services.topic.ReadTopicFeedResponse
import com.echoed.chamber.services.echoeduser.FollowUser
import com.echoed.chamber.services.partner.RequestPartnerContentFeedResponse
import com.echoed.chamber.services.topic.ReadTopicsResponse
import com.echoed.chamber.services.echoeduser.PublishFacebookAction


@Controller
@RequestMapping(Array("/api"))
class ApiController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value = Array("/category/{categoryId}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def categoryFeed(
            @PathVariable(value = "categoryId") categoryId: String,
            @RequestParam(value = "page", required = false) page: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        val result = new DeferredResult[ContentFeed[PublicContext]](null, ErrorResult.timeout)

        log.debug("Requesting for Category Feed for Category {}", categoryId )

        mp(GetCategoryStoryFeed(categoryId, parse(page))).onSuccess {
            case GetCategoryStoryFeedResponse(_, Right(feed)) => result.setResult(feed)
        }

        result
    }

    @RequestMapping(value = Array("/topic/{topicId}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def topicFeed(
            @PathVariable(value = "topicId") topicId: String,
            @RequestParam(value = "page", required = false) page: String) = {
        val result = new DeferredResult[ContentFeed[TopicContext]](null, ErrorResult.timeout)

        log.debug("Requesting Topic Feed for Topic {}", topicId)
        mp(ReadTopicFeed(topicId, parse(page))).onSuccess {
            case ReadTopicFeedResponse(_, Right(feed)) => result.setResult(feed)
        }
        result
    }

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

    @RequestMapping(value = Array("/topics"), method = Array(RequestMethod.GET))
    @ResponseBody
    def topics = {
        val result = new DeferredResult[List[Topic]](null, ErrorResult.timeout)

        mp(ReadTopics()).onSuccess {
            case ReadTopicsResponse(_, Right(topics)) => result.setResult(topics)
        }

        result
    }

    @RequestMapping(value = Array("/topics/partner/{id}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def topics(@PathVariable(value = "id") partnerId: String)= {
        val result = new DeferredResult[List[Topic]](null, ErrorResult.timeout)

        mp(GetTopics(new PartnerClientCredentials(partnerId))).onSuccess {
            case GetTopicsResponse(_, Right(topics)) => result.setResult(topics)
        }
        result
    }

    @RequestMapping(value = Array("/topics/community/{id}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def communityTopics(@PathVariable(value = "id") communityId: String) = {

        val result = new DeferredResult[List[Topic]](null, ErrorResult.timeout)
        mp(ReadCommunityTopics(communityId)).onSuccess {
            case ReadCommunityTopicsResponse(_, Right(topics)) => result.setResult(topics)
        }
        result
    }

}