package com.echoed.chamber.controllers.api

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
import views.ClosetPersonal
import com.echoed.chamber.services.partner._
import views.CommunityFeed
import views.ContentFeed
import views.PublicStoryFeed
import com.echoed.chamber.domain.views.context._
import com.echoed.chamber.services.feed.GetStoryResponse
import com.echoed.chamber.services.echoeduser.FetchNotifications
import com.echoed.chamber.services.echoeduser.ReadSettingsResponse
import com.echoed.chamber.services.echoeduser.UnFollowPartnerResponse
import com.echoed.chamber.services.partner.GetTopicsResponse
import com.echoed.chamber.services.echoeduser.UnFollowPartner
import com.echoed.chamber.services.echoeduser.ListFollowedByUsers
import com.echoed.chamber.services.partner.GetTopics
import com.echoed.chamber.services.echoeduser.NewSettingsResponse
import views.TopicStoryFeed
import com.echoed.chamber.services.topic.ReadTopicFeed
import com.echoed.chamber.services.feed.GetCommunities
import com.echoed.chamber.services.feed.GetStory
import com.echoed.chamber.domain.Vote
import com.echoed.chamber.services.echoeduser.MarkNotificationsAsRead
import com.echoed.chamber.domain.EchoedUserSettings
import com.echoed.chamber.services.echoeduser.NewSettings
import com.echoed.chamber.services.echoeduser.ListFollowingPartnersResponse
import com.echoed.chamber.services.echoeduser.GetExhibit
import com.echoed.chamber.services.echoeduser.FollowPartnerResponse
import com.echoed.chamber.services.echoeduser.PartnerFollower
import com.echoed.chamber.services.partner.PartnerClientCredentials
import com.echoed.chamber.services.topic.ReadTopics
import com.echoed.chamber.services.echoeduser.UnFollowUserResponse
import com.echoed.chamber.services.echoeduser.FollowUserResponse
import com.echoed.chamber.services.feed.GetCommunitiesResponse
import com.echoed.chamber.services.echoeduser.VoteStoryResponse
import com.echoed.chamber.services.echoeduser.UnFollowUser
import com.echoed.chamber.services.feed.GetPublicStoryFeed
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
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.domain.Topic
import com.echoed.chamber.services.echoeduser.ReadSettings
import com.echoed.chamber.services.feed.GetPublicStoryFeedResponse
import com.echoed.chamber.services.echoeduser.GetExhibitResponse
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
import com.echoed.chamber.services.topic.ReadTopicsResponse
import com.echoed.chamber.services.echoeduser.PublishFacebookAction
import com.echoed.chamber.services.echoeduser.{ RequestUserContentFeed, RequestUserContentFeedResponse, RequestOwnContent, RequestOwnContentResponse }


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
    def customFeed(
            @RequestParam(value = "page", required = false) page: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[ContentFeed[SelfContext]](null, ErrorResult.timeout)

        mp(RequestCustomUserFeed(eucc, parse(page))).onSuccess {
            case RequestCustomUserFeedResponse(_, Right(sf)) =>
                result.setResult(sf)
        }

        result
    }

    @RequestMapping(value = Array("/public/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def publicFeed(@RequestParam(value = "page", required = false) page: String): DeferredResult[PublicStoryFeed] = {

        val result = new DeferredResult[PublicStoryFeed](null, ErrorResult.timeout)

        mp(GetPublicStoryFeed(parse(page))).onSuccess {
            case GetPublicStoryFeedResponse(_, Right(feed)) => result.setResult(feed)
        }

        result
    }

    @RequestMapping(value = Array("me/exhibit"), method = Array(RequestMethod.GET))
    @ResponseBody
    def ownFeed(
            @RequestParam(value = "page", required = false) page: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[ContentFeed[SelfContext]](null, ErrorResult.timeout)
        mp(RequestOwnContent(eucc, parse(page), "story")).onSuccess {
            case RequestOwnContentResponse(_, Right(cf)) =>
                result.setResult(cf)
        }
        result

    }

    @RequestMapping(value = Array("me/following/partners"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowingPartners(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[List[PartnerFollower]](null, ErrorResult.timeout)
        mp(ListFollowingPartners(eucc)).onSuccess {
            case ListFollowingPartnersResponse(_, Right(fp)) => result.setResult(fp)
        }
        result
    }

    @RequestMapping(value = Array("me/following/partners/{partnerToFollowId}"), method = Array(RequestMethod.PUT))
    @ResponseBody
    def followPartner(
            @PathVariable(value = "partnerToFollowId") partnerToFollowId: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[List[PartnerFollower]](null, ErrorResult.timeout)

        mp(FollowPartner(eucc, partnerToFollowId)).onSuccess {
            case FollowPartnerResponse(_, Right(fp)) => result.setResult(fp)
        }
        result

    }

    @RequestMapping(value = Array("me/following/partners/{partnerToUnFollowId}"), method = Array(RequestMethod.DELETE))
    @ResponseBody
    def unFollowPartner(
                         @PathVariable(value = "partnerToUnFollowId") partnerToUnFollowId: String,
                         eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[List[PartnerFollower]](null, ErrorResult.timeout)

        mp(UnFollowPartner(eucc, partnerToUnFollowId)).onSuccess {
            case UnFollowPartnerResponse(_, Right(fp)) => result.setResult(fp)
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
    @ResponseBody
    def followUser(
            @PathVariable(value = "userToFollowId") userToFollowId: String,
            response: HttpServletResponse,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[List[Follower]](null, ErrorResult.timeout)

        mp(FollowUser(eucc, userToFollowId)).onSuccess{
            case FollowUserResponse(_, Right(fus)) => result.setResult(fus)
        }
        result
    }

    @RequestMapping(value = Array("/me/following/{userToUnFollowId}"), method = Array(RequestMethod.DELETE))
    @ResponseBody
    def unFollowUser(
            @PathVariable(value = "userToUnFollowId") userToUnFollowId: String,
            response: HttpServletResponse,
            eucc: EchoedUserClientCredentials)  = {

        val result = new DeferredResult[List[Follower]](null, ErrorResult.timeout)
        mp(UnFollowUser(eucc, userToUnFollowId)).onSuccess {
            case UnFollowUserResponse(_, Right(fus)) =>
                result.setResult(fus)
        }
        result
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

        val result = new DeferredResult[ContentFeed[PartnerContext]](null, ErrorResult.timeout)

        log.debug("Requesting for Partner Feed for Partner {}", partnerId )

        mp(ReadPartnerFeed(new PartnerClientCredentials(partnerId), parse(page), origin)).onSuccess {
            case ReadPartnerFeedResponse(_, Right(partnerFeed)) => result.setResult(partnerFeed)
        }

        result
    }

    @RequestMapping(value = Array("/partner/{partnerId}/photos"), method=Array(RequestMethod.GET))
    @ResponseBody
    def partnerFeedPhotos(
                       @PathVariable(value = "partnerId") partnerId: String,
                       @RequestParam(value = "page", required = false) page: String,
                       @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        val result = new DeferredResult[ContentFeed[PartnerContext]](null, ErrorResult.timeout)

        log.debug("Requesting for Partner Feed for Partner {}", partnerId )

        mp(RequestPartnerContentFeed(new PartnerClientCredentials(partnerId), parse(page), origin, "photo")).onSuccess {
            case RequestPartnerContentFeedResponse(_, Right(partnerFeed)) => result.setResult(partnerFeed)
        }
        result
    }

    @RequestMapping(value= Array("/user/{id}"), method=Array(RequestMethod.GET))
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

    @RequestMapping(value= Array("/user/{id}/photos"), method=Array(RequestMethod.GET))
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