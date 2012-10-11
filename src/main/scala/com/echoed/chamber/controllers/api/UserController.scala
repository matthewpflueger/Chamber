package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import com.echoed.chamber.services.echoeduser._
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.feed._
import com.echoed.chamber.services.tag._
import scala.util.control.Exception._
import java.lang.{NumberFormatException => NFE}
import javax.annotation.Nullable


@Controller
@RequestMapping(Array("/api"))
class UserController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value = Array("/notifications"), method = Array(RequestMethod.GET))
    @ResponseBody
    def fetchNotifications(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(FetchNotifications(eucc)).onSuccess {
            case FetchNotificationsResponse(_, Right(notifications)) => result.set(notifications)
        }

        result
    }

    @RequestMapping(value = Array("/notifications"), method = Array(RequestMethod.POST))
    @ResponseBody
    def readNotifications(
            @RequestParam(value = "ids", required = true) ids: Array[String],
            eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(MarkNotificationsAsRead(eucc, ids.toSet)).onSuccess {
            case MarkNotificationsAsReadResponse(_, Right(boolean)) => result.set(boolean)
        }

        result
    }


    @RequestMapping(value = Array("/me/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def readSettings(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(ReadSettings(eucc)).onSuccess {
            case ReadSettingsResponse(_, Right(eus)) => result.set(eus)
        }

        result
    }


    @RequestMapping(value = Array("/me/settings"), method = Array(RequestMethod.POST))
    @ResponseBody
    def newSettings(
            @RequestBody(required = true) settings: Map[String, AnyRef],
            eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(NewSettings(eucc, settings)).onSuccess {
            case NewSettingsResponse(_, Right(eus)) => result.set(eus)
        }

        result
    }


    @RequestMapping(value = Array("/me/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def publicFeed(@RequestParam(value = "page", required = false) page: String) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetPublicStoryFeed(parse(page))).onSuccess {
            case GetPublicStoryFeedResponse(_, Right(feed)) => result.set(feed)
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

        val result = new DeferredResult(ErrorResult.timeout)

        //FIXME WTF this returns the user's feed, not a friend's feed?!?!
        mp(GetFeed(eucc, parse(page))).onSuccess {
            case GetFeedResponse(_, Right(feed)) => result.set(feed)
        }

        result
    }

    @RequestMapping(value = Array("/me/exhibit"), method = Array(RequestMethod.GET))
    @ResponseBody
    def exhibit(
            @RequestParam(value = "page", required = false) page: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetExhibit(eucc, parse(page))).onSuccess {
            case GetExhibitResponse(_, Right(closet)) =>
                log.debug("Received for {} exhibit of {} echoes", eucc, closet.echoes.size)
                result.set(closet)
        }

        result
    }

    @RequestMapping(value = Array("/me/friends"), method = Array(RequestMethod.GET))
    @ResponseBody
    def friends(
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetEchoedFriends(eucc)).onSuccess {
            case GetEchoedFriendsResponse(_, Right(friends)) => result.set(friends)
        }

        result
    }

    @RequestMapping(value = Array("/me/following"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowingUsers(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(ListFollowingUsers(eucc)).onSuccess {
            case ListFollowingUsersResponse(_, Right(fus)) => result.set(fus)
        }

        result
    }

    @RequestMapping(value = Array("/me/followers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowedByUsers(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(ListFollowedByUsers(eucc)).onSuccess {
            case ListFollowedByUsersResponse(_, Right(fbu)) => result.set(fbu)
        }

        result
    }

    @RequestMapping(value = Array("/me/following/{userToFollowId}"), method = Array(RequestMethod.PUT))
    @ResponseBody
    def followUser(
            @PathVariable(value = "userToFollowId") userToFollowId: String,
            eucc: EchoedUserClientCredentials) = {
        mp(FollowUser(eucc, userToFollowId))
        true
    }

    @RequestMapping(value = Array("/me/following/{userToUnFollowId}"), method = Array(RequestMethod.DELETE))
    @ResponseBody
    def unFollowUser(
            @PathVariable(value = "userToUnFollowId") userToUnFollowId: String,
            eucc: EchoedUserClientCredentials) = {
        mp(UnFollowUser(eucc, userToUnFollowId))
        true
    }


    @RequestMapping(value = Array("/category/{categoryId}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def categoryFeed(
            @PathVariable(value = "categoryId") categoryId: String,
            @RequestParam(value = "page", required = false) page: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        val result = new DeferredResult(ErrorResult.timeout)

        log.debug("Requesting for Category Feed for Category {}", categoryId )

        mp(GetCategoryStoryFeed(categoryId, parse(page))).onSuccess {
            case GetCategoryStoryFeedResponse(_, Right(feed)) => result.set(feed)
        }

        result
    }


    @RequestMapping(value = Array("/partner/{partnerId}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def partnerFeed(
            @PathVariable(value = "partnerId") partnerId: String,
            @RequestParam(value = "page", required = false) page: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        val result = new DeferredResult(ErrorResult.timeout)

        log.debug("Requesting for Partner Feed for Partner {}", partnerId )

        mp(GetPartnerStoryFeed(partnerId, parse(page), origin)).onSuccess {
            case GetPartnerStoryFeedResponse(_, Right(partnerFeed)) => result.set(partnerFeed)
        }

        result
    }

    @RequestMapping(value= Array("/user/{id}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def friendExhibit(
            @PathVariable(value ="id") echoedFriendId: String,
            @RequestParam(value = "page", required = false) page: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        log.debug("echoedFriendId: {}", echoedFriendId)

        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetUserPublicStoryFeed(echoedFriendId, parse(page))).onSuccess {
            case GetUserPublicStoryFeedResponse(_, Right(feed)) => result.set(feed)
        }

        result
    }

    @RequestMapping(value = Array("/story/{id}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getStory(
            @PathVariable(value = "id") id: String,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        log.debug("Requesting Story {}", id )

        mp(GetStory(id, origin)).onSuccess {
            case GetStoryResponse(_, Right(story)) => result.set(story)
        }

        Option(eucc).map(c => mp(PublishFacebookAction(c, "browse", "story", v.storyGraphUrl + id)))
        result
    }

    @RequestMapping(value = Array("/tags"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getTagList(
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin : String) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetCommunities()).onSuccess {
            case GetCommunitiesResponse(_, Right(communities)) =>
                log.debug("Communities Returned: {}", communities)
                result.set(communities)

        }

        result
    }

    @RequestMapping(value = Array("/upvote"), method = Array(RequestMethod.GET))
    @ResponseBody
    def upVote(
              eucc: EchoedUserClientCredentials,
              @RequestParam(value = "storyId", required = true) storyId: String,
              @RequestParam(value = "storyOwnerId", required = true) storyOwnerId: String) = {
        mp(VoteStory(eucc, storyOwnerId, storyId, 1))
        true
    }

    @RequestMapping(value = Array("/downvote"), method = Array(RequestMethod.GET))
    @ResponseBody
    def downVote(
                eucc: EchoedUserClientCredentials,
                @RequestParam(value = "storyId", required = true) storyId: String,
                @RequestParam(value = "storyOwnerId", required = true) storyOwnerId: String) = {
        mp(VoteStory(eucc, storyOwnerId, storyId, -1))
        true
    }


    @RequestMapping(value = Array("/community/update"), method = Array(RequestMethod.GET))
    @ResponseBody
    def updateCommunity(
        eucc: EchoedUserClientCredentials,
        @RequestParam(value = "communityId", required = true) communityId: String,
        @RequestParam(value = "storyId", required = true) storyId: String) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(UpdateCommunity(eucc, storyId, communityId)).onSuccess {
            case UpdateCommunityResponse(_, Right(story)) => result.set(story)
        }
        result
    }

}