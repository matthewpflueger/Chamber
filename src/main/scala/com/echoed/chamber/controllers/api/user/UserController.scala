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
import com.echoed.chamber.domain.views.content.{ContentDescription, Content}
import com.echoed.chamber.services.echoeduser._
import views.Feed
import views.context.UserContext
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.echoeduser.RequestUserContentFeedResponse
import com.echoed.chamber.services.echoeduser.RequestUserContentFeed


@Controller
@RequestMapping(Array("/api/user"))
class UserController extends EchoedController {

    @RequestMapping(value = Array("/{id}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getUserDefaultContent(
                    @PathVariable(value ="id") id: String,
                    @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
                    @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) =
        getUserContent(id, Content.defaultContentDescription, page, origin)

    @RequestMapping(value = Array("/{id}/{contentType}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getUserOtherContent(
            @PathVariable(value ="id") id: String,
            @PathVariable(value = "contentType") contentType: String,
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) =
        getUserContent(id, Content.getContentDescription(contentType), page, origin)


    def getUserContent(id: String, contentDescription: ContentDescription, page: Int, origin: String) = {
        log.debug("Getting feed for {}", id)

        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)

        mp(RequestUserContentFeed(
                new EchoedUserClientCredentials(id),
                contentDescription,
                Option(page))).onSuccess {
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

    @RequestMapping(value = Array("/{id}/following/partners"), method = Array(RequestMethod.GET))
    @ResponseBody
    def listFollowingPartners(@PathVariable(value ="id") id: String) = {
        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)

        mp(RequestPartnersFollowed(new EchoedUserClientCredentials(id))).onSuccess {
            case RequestPartnersFollowedResponse(_, Right(fus)) => result.setResult(fus)
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