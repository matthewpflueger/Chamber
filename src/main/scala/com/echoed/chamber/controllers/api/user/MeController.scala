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
@RequestMapping(Array("/api/me"))
class MeController extends EchoedController {


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

    @RequestMapping(value = Array("/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def readSettings(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[EchoedUserSettings](null, ErrorResult.timeout)

        mp(ReadSettings(eucc)).onSuccess {
            case ReadSettingsResponse(_, Right(eus)) => result.setResult(eus)
        }

        result
    }


    @RequestMapping(value = Array("/settings"), method = Array(RequestMethod.POST))
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


    @RequestMapping(value = Array("/feed", "/feed/stories"), method = Array(RequestMethod.GET))
    @ResponseBody
    def customFeed(
                      @RequestParam(value = "page", required = false) page: String,
                      eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[ContentFeed[PersonalizedContext]](null, ErrorResult.timeout)
        mp(RequestCustomUserFeed(eucc, parse(page), "story")).onSuccess {
            case RequestCustomUserFeedResponse(_, Right(sf)) =>
                result.setResult(sf)
        }
        result
    }

    @RequestMapping(value = Array("/feed/photos"), method = Array(RequestMethod.GET))
    @ResponseBody
    def photoFeed(
                    @RequestParam(value = "page", required = false) page: String,
                    eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[ContentFeed[PersonalizedContext]](null, ErrorResult.timeout)
        mp(RequestCustomUserFeed(eucc, parse(page), "photo")).onSuccess {
            case RequestCustomUserFeedResponse(_, Right(sf)) =>
                result.setResult(sf)
        }
        result
    }

    @RequestMapping(value = Array("", "/stories"), method = Array(RequestMethod.GET))
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

    @RequestMapping(value = Array("/photos"), method = Array(RequestMethod.GET))
    @ResponseBody
    def ownContent(
                      @RequestParam(value = "page", required = false) page: String,
                      eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[ContentFeed[SelfContext]](null, ErrorResult.timeout)
        mp(RequestOwnContent(eucc, parse(page), "photo")).onSuccess {
            case RequestOwnContentResponse(_, Right(cf)) =>
                result.setResult(cf)
        }
        result
    }

    @RequestMapping(value = Array("/following/partners"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowingPartners(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[List[PartnerFollower]](null, ErrorResult.timeout)
        mp(ListFollowingPartners(eucc)).onSuccess {
            case ListFollowingPartnersResponse(_, Right(fp)) => result.setResult(fp)
        }
        result
    }

    @RequestMapping(value = Array("/following/partners/{partnerToFollowId}"), method = Array(RequestMethod.PUT))
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

    @RequestMapping(value = Array("/following/partners/{partnerToUnFollowId}"), method = Array(RequestMethod.DELETE))
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

    @RequestMapping(value = Array("/following"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowingUsers(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[List[Follower]](null, ErrorResult.timeout)

        mp(ListFollowingUsers(eucc)).onSuccess {
            case ListFollowingUsersResponse(_, Right(fus)) => result.setResult(fus)
        }
        result
    }

    @RequestMapping(value = Array("/followers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowedByUsers(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[List[Follower]](null, ErrorResult.timeout)

        mp(ListFollowedByUsers(eucc)).onSuccess {
            case ListFollowedByUsersResponse(_, Right(fbu)) => result.setResult(fbu)
        }

        result
    }

    @RequestMapping(value = Array("/following/{userToFollowId}"), method = Array(RequestMethod.PUT))
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

    @RequestMapping(value = Array("/following/{userToUnFollowId}"), method = Array(RequestMethod.DELETE))
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


}