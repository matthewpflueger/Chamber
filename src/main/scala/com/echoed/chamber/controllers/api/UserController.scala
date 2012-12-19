package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.async.DeferredResult
import scala.util.control.Exception._
import java.lang.{NumberFormatException => NFE}
import javax.annotation.Nullable
import com.echoed.chamber.services.partner._
import com.echoed.chamber.services.feed.GetCommunitiesResponse
import com.echoed.chamber.services.feed.GetStoryResponse
import com.echoed.chamber.services.echoeduser.{Follower, GetFeedResponse, FetchNotifications, UnFollowUser, GetUserFeedResponse, ReadSettingsResponse, ListFollowedByUsers, NewSettingsResponse, MarkNotificationsAsReadResponse, ListFollowedByUsersResponse, ReadSettings, GetExhibitResponse, FetchNotificationsResponse, ListFollowingUsers, GetFeed, MarkNotificationsAsRead, ListFollowingUsersResponse, GetUserFeed, EchoedUserClientCredentials, NewSettings, VoteStory, GetExhibit, FollowUser, PublishFacebookAction}
import com.echoed.chamber.services.feed.GetPublicStoryFeed
import com.echoed.chamber.services.topic.ReadTopicsResponse
import com.echoed.chamber.services.feed.GetCategoryStoryFeedResponse
import com.echoed.chamber.services.topic.ReadTopics
import com.echoed.chamber.services.feed.GetCategoryStoryFeed
import com.echoed.chamber.services.topic.ReadCommunityTopicsResponse
import scala.Right
import com.echoed.chamber.services.partner.ReadPartnerFeed
import com.echoed.chamber.services.topic.ReadTopicFeed
import com.echoed.chamber.services.feed.GetCommunities
import com.echoed.chamber.services.feed.GetStory
import com.echoed.chamber.services.feed.GetPublicStoryFeedResponse
import com.echoed.chamber.services.topic.ReadCommunityTopics
import com.echoed.chamber.services.topic.ReadTopicFeedResponse
import com.echoed.chamber.services.partner.PartnerClientCredentials
import com.echoed.chamber.services.echoeduser.PublishFacebookAction
import javax.servlet.http.HttpServletResponse
import com.echoed.chamber.domain.{Topic, EchoedUserSettings, Notification}
import scala.collection.immutable.Stack
import com.echoed.chamber.domain.views.{CommunityFeed, EchoedUserStoryFeed, PartnerStoryFeed, TopicStoryFeed, ClosetPersonal, Feed, PublicStoryFeed}
import com.echoed.chamber.domain.public.StoryPublic


@Controller
@RequestMapping(Array("/api"))
class UserController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value = Array("/notifications"), method = Array(RequestMethod.GET))
    @ResponseBody
    def fetchNotifications(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[Stack[Notification]](null, ErrorResult.timeout)

        mp(FetchNotifications(eucc)).onSuccess {
            case FetchNotificationsResponse(_, Right(notifications)) => result.setResult(notifications)
        }

        result
    }

    @RequestMapping(value = Array("/notifications"), method = Array(RequestMethod.POST))
    @ResponseBody
    def readNotifications(
            @RequestParam(value = "ids", required = true) ids: Array[String],
            eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[Boolean](null, ErrorResult.timeout)

        mp(MarkNotificationsAsRead(eucc, ids.toSet)).onSuccess {
            case MarkNotificationsAsReadResponse(_, Right(boolean)) => result.setResult(boolean)
        }

        result
    }


    @RequestMapping(value = Array("/me/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def readSettings(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[EchoedUserSettings](null, ErrorResult.timeout)

        mp(ReadSettings(eucc)).onSuccess {
            case ReadSettingsResponse(_, Right(eus)) => result.setResult(eus)
        }

        result
    }


    @RequestMapping(value = Array("/me/settings"), method = Array(RequestMethod.POST))
    @ResponseBody
    def newSettings(
            @RequestBody(required = true) settings: Map[String, AnyRef],
            eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[EchoedUserSettings](null, ErrorResult.timeout)

        mp(NewSettings(eucc, settings)).onSuccess {
            case NewSettingsResponse(_, Right(eus)) => result.setResult(eus)
        }

        result
    }


    @RequestMapping(value = Array("/me/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def publicFeed(@RequestParam(value = "page", required = false) page: String): DeferredResult[PublicStoryFeed] = {

        val result = new DeferredResult[PublicStoryFeed](null, ErrorResult.timeout)

        mp(GetPublicStoryFeed(parse(page))).onSuccess {
            case GetPublicStoryFeedResponse(_, Right(feed)) => result.setResult(feed)
        }

        result
    }

    @RequestMapping(value = Array("/feed/friends"), method = Array(RequestMethod.GET))
    @ResponseBody
    def feed(
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam:String,
            @RequestParam(value = "page", required = false) page: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[Feed](null, ErrorResult.timeout)

        //FIXME WTF this returns the user's feed, not a friend's feed?!?!
        mp(GetFeed(eucc, parse(page))).onSuccess {
            case GetFeedResponse(_, Right(feed)) => result.setResult(feed)
        }

        result
    }

    @RequestMapping(value = Array("/me/exhibit"), method = Array(RequestMethod.GET))
    @ResponseBody
    def exhibit(
            @RequestParam(value = "page", required = false) page: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[ClosetPersonal](null, ErrorResult.timeout)

        mp(GetExhibit(eucc, parse(page))).onSuccess {
            case GetExhibitResponse(_, Right(closet)) =>
                log.debug("Received for {} exhibit of {} echoes", eucc, closet.echoes.size)
                result.setResult(closet)
        }

        result
    }

    @RequestMapping(value = Array("/me/following"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowingUsers(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[List[Follower]](null, ErrorResult.timeout)

        mp(ListFollowingUsers(eucc)).onSuccess {
            case ListFollowingUsersResponse(_, Right(fus)) => result.setResult(fus)
        }

        result
    }

    @RequestMapping(value = Array("/me/followers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowedByUsers(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[List[Follower]](null, ErrorResult.timeout)

        mp(ListFollowedByUsers(eucc)).onSuccess {
            case ListFollowedByUsersResponse(_, Right(fbu)) => result.setResult(fbu)
        }

        result
    }

    @RequestMapping(value = Array("/me/following/{userToFollowId}"), method = Array(RequestMethod.PUT))
    def followUser(
            @PathVariable(value = "userToFollowId") userToFollowId: String,
            response: HttpServletResponse,
            eucc: EchoedUserClientCredentials) {
        mp(FollowUser(eucc, userToFollowId))
        response.setStatus(200)
    }

    @RequestMapping(value = Array("/me/following/{userToUnFollowId}"), method = Array(RequestMethod.DELETE))
    def unFollowUser(
            @PathVariable(value = "userToUnFollowId") userToUnFollowId: String,
            response: HttpServletResponse,
            eucc: EchoedUserClientCredentials) {
        mp(UnFollowUser(eucc, userToUnFollowId))
        response.setStatus(200)
    }


    @RequestMapping(value = Array("/category/{categoryId}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def categoryFeed(
            @PathVariable(value = "categoryId") categoryId: String,
            @RequestParam(value = "page", required = false) page: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        val result = new DeferredResult[PublicStoryFeed](null, ErrorResult.timeout)

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
        val result = new DeferredResult[TopicStoryFeed](null, ErrorResult.timeout)

        log.debug("Requesting Topic Feed for Topic {}", topicId)
        mp(ReadTopicFeed(topicId, parse(page))).onSuccess {
            case ReadTopicFeedResponse(_, Right(feed)) => result.setResult(feed)
        }
        result
    }

    @RequestMapping(value = Array("/partner/{partnerId}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def partnerFeed(
            @PathVariable(value = "partnerId") partnerId: String,
            @RequestParam(value = "page", required = false) page: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        val result = new DeferredResult[PartnerStoryFeed](null, ErrorResult.timeout)

        log.debug("Requesting for Partner Feed for Partner {}", partnerId )

        mp(ReadPartnerFeed(new PartnerClientCredentials(partnerId), parse(page), origin)).onSuccess {
            case ReadPartnerFeedResponse(_, Right(partnerFeed)) => result.setResult(partnerFeed)
        }

        result
    }

    @RequestMapping(value= Array("/user/{id}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def friendExhibit(
            @PathVariable(value ="id") id: String,
            @RequestParam(value = "page", required = false) page: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        log.debug("Getting feed for {}", id)

        val result = new DeferredResult[EchoedUserStoryFeed](null, ErrorResult.timeout)

        mp(GetUserFeed(new EchoedUserClientCredentials(id), parse(page))).onSuccess {
            case GetUserFeedResponse(_, Right(feed)) => result.setResult(feed)
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

    @RequestMapping(value = Array("/tags"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getTagList(
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin : String) = {

        val result = new DeferredResult[CommunityFeed](null, ErrorResult.timeout)

        mp(GetCommunities()).onSuccess {
            case GetCommunitiesResponse(_, Right(communities)) =>
                result.setResult(communities)

        }

        result
    }

    @RequestMapping(value = Array("/upvote"), method = Array(RequestMethod.GET))
    def upVote(
              eucc: EchoedUserClientCredentials,
              response: HttpServletResponse,
              @RequestParam(value = "storyId", required = true) storyId: String,
              @RequestParam(value = "storyOwnerId", required = true) storyOwnerId: String) {
        mp(VoteStory(eucc, storyOwnerId, storyId, 1))
        response.setStatus(200)
    }

    @RequestMapping(value = Array("/downvote"), method = Array(RequestMethod.GET))
    def downVote(
                eucc: EchoedUserClientCredentials,
                response: HttpServletResponse,
                @RequestParam(value = "storyId", required = true) storyId: String,
                @RequestParam(value = "storyOwnerId", required = true) storyOwnerId: String) {
        mp(VoteStory(eucc, storyOwnerId, storyId, -1))
        response.setStatus(200)
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