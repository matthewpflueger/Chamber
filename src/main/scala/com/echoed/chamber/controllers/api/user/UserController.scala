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
import views.content.PhotoContent
import views.Feed
import com.echoed.chamber.domain.views.context._
import com.echoed.chamber.services.echoeduser._
import views.Feed
import views.Feed
import views.Feed
import views.Feed
import views.context.UserContext
import views.context.UserContext
import views.context.UserContext
import com.echoed.chamber.services.echoeduser.ListFollowingUsers
import com.echoed.chamber.services.echoeduser.ListFollowingUsersResponse
import views.context.UserContext
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.echoeduser.RequestUserContentFeedResponse
import com.echoed.chamber.services.echoeduser.ListFollowedByUsers
import com.echoed.chamber.services.echoeduser.RequestUserContentFeed
import com.echoed.chamber.domain.public.StoryPublic


@Controller
@RequestMapping(Array("/api/user"))
class UserController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value= Array("/{id}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def getUserContent(
                    @PathVariable(value ="id") id: String,
                    @RequestParam(value = "page", required = false) page: String,
                    @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        log.debug("Getting feed for {}", id)

        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)

        mp(RequestUserContentFeed(new EchoedUserClientCredentials(id), parse(page), classOf[StoryPublic])).onSuccess {
            case RequestUserContentFeedResponse(_, Right(feed)) => result.setResult(feed)
        }

        result
    }

    @RequestMapping(value= Array("/{id}/photos"), method=Array(RequestMethod.GET))
    @ResponseBody
    def getUserContentPhotos(
                         @PathVariable(value ="id") id: String,
                         @RequestParam(value = "page", required = false) page: String,
                         @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        log.debug("Getting feed for {}", id)

        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)

        mp(RequestUserContentFeed(new EchoedUserClientCredentials(id), parse(page), classOf[PhotoContent])).onSuccess {
            case RequestUserContentFeedResponse(_, Right(feed)) => result.setResult(feed)
        }

        result
    }

    @RequestMapping(value = Array("/{id}/following"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getUserFollowing(@PathVariable(value ="id") id: String) = {
        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)

        mp(RequestUsersFollowed(new EchoedUserClientCredentials(id))).onSuccess {
            case RequestUsersFollowedResponse(_, Right(fus)) => result.setResult(fus)
        }
        result
    }

    @RequestMapping(value = Array("/{id}/followers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getUserFollowers(@PathVariable(value ="id") id: String) = {
        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)

        mp(RequestFollowers(new EchoedUserClientCredentials(id))).onSuccess {
            case RequestFollowersResponse(_, Right(fbu)) => result.setResult(fbu)
        }
        result
    }

    @RequestMapping(value = Array("/{id}/followers"), method = Array(RequestMethod.PUT))
    @ResponseBody
    def putUserFollower(
        @PathVariable(value = "id") id: String,
        eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[List[Follower]](null, ErrorResult.timeout)

        mp(FollowUser(eucc, id)).onSuccess{
            case FollowUserResponse(_, Right(fus)) => result.setResult(fus)
        }
        result
    }

    @RequestMapping(value = Array("/{id}/followers"), method = Array(RequestMethod.DELETE))
    @ResponseBody
    def deleteUserFollower(
        @PathVariable(value = "id") id: String,
        eucc: EchoedUserClientCredentials)  = {

        val result = new DeferredResult[List[Follower]](null, ErrorResult.timeout)
        mp(UnFollowUser(eucc, id)).onSuccess {
            case UnFollowUserResponse(_, Right(fus)) =>
                result.setResult(fus)
        }
        result
    }

}