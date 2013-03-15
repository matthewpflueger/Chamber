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
import com.echoed.chamber.domain.views.content.{Content, ContentDescription, PhotoContent}
import views.Feed
import com.echoed.chamber.domain.views.context._
import com.echoed.chamber.services.echoeduser.FetchNotifications
import com.echoed.chamber.services.echoeduser.ReadSettingsResponse
import com.echoed.chamber.services.echoeduser.NewSettingsResponse
import com.echoed.chamber.services.echoeduser.MarkNotificationsAsRead
import com.echoed.chamber.domain.EchoedUserSettings
import com.echoed.chamber.services.echoeduser.RequestOwnContentResponse
import com.echoed.chamber.services.echoeduser.NewSettings
import com.echoed.chamber.services.echoeduser.PartnerFollower
import com.echoed.chamber.services.echoeduser.RequestCustomUserFeed
import com.echoed.chamber.services.echoeduser.RequestCustomUserFeedResponse
import com.echoed.chamber.services.echoeduser.MarkNotificationsAsReadResponse
import com.echoed.chamber.services.echoeduser.RequestOwnContent
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.services.echoeduser.ReadSettings
import com.echoed.chamber.services.echoeduser.FetchNotificationsResponse
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials


@Controller
@RequestMapping(Array("/api/me"))
class MeController extends EchoedController {


    private val failAsZero = failAsValue(classOf[NFE])(0)
    private def parse(number: String) =  failAsZero { Integer.parseInt(number) }

    @RequestMapping(value = Array("/notifications"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getNotifications(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[Stack[Notification]](null, ErrorResult.timeout)

        mp(FetchNotifications(eucc)).onSuccess {
            case FetchNotificationsResponse(_, Right(notifications)) => result.setResult(notifications)
        }
        result
    }

    @RequestMapping(value = Array("/notifications"), method = Array(RequestMethod.POST))
    @ResponseBody
    def postNotifications(
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
    def getSettings(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[EchoedUserSettings](null, ErrorResult.timeout)

        mp(ReadSettings(eucc)).onSuccess {
            case ReadSettingsResponse(_, Right(eus)) => result.setResult(eus)
        }

        result
    }


    @RequestMapping(value = Array("/settings"), method = Array(RequestMethod.POST))
    @ResponseBody
    def postSettings(
            @RequestBody(required = true) settings: Map[String, AnyRef],
            eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[EchoedUserSettings](null, ErrorResult.timeout)

        mp(NewSettings(eucc, settings)).onSuccess {
            case NewSettingsResponse(_, Right(eus)) => result.setResult(eus)
        }
        result
    }


    @RequestMapping(value = Array("/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getFeedStories(
            @RequestParam(value = "page", required = false) page: String,
            eucc: EchoedUserClientCredentials) = {
        getFeedContent(Content.defaultContentDescription, page, eucc)
    }

    @RequestMapping(value = Array("/feed/{contentType}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getFeed(
            @PathVariable(value = "contentType") contentType: String,
            @RequestParam(value = "page", required = false) page: String,
            eucc: EchoedUserClientCredentials) = {
        getFeedContent(Content.getContentDescription(contentType), page, eucc)
    }

    def getFeedContent(contentType: ContentDescription,
                       page: String,
                       eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[Feed[PersonalizedContext]](null, ErrorResult.timeout)
        mp(RequestCustomUserFeed(eucc, parse(page), contentType)).onSuccess {
            case RequestCustomUserFeedResponse(_, Right(sf)) =>
                result.setResult(sf)
        }
        result
    }


    @RequestMapping(method = Array(RequestMethod.GET))
    @ResponseBody
    def getOwnStories(
            @RequestParam(value = "page", required = false) page: String,
            eucc: EchoedUserClientCredentials) = {
        getOwnContent(Content.defaultContentDescription, page, eucc)
    }

    @RequestMapping(value = Array("/{contentType}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getOwn(
            @PathVariable(value = "contentType") contentType: String,
            @RequestParam(value = "page", required = false) page: String,
            eucc: EchoedUserClientCredentials) = {

        getOwnContent(Content.getContentDescription(contentType), page, eucc)
    }

    def getOwnContent(contentType: ContentDescription,
                      page: String,
                      eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[Feed[SelfContext]](null, ErrorResult.timeout)
        mp(RequestOwnContent(eucc, parse(page), contentType)).onSuccess {
            case RequestOwnContentResponse(_, Right(cf)) =>
                result.setResult(cf)
        }
        result
    }

    @RequestMapping(value = Array("/following/partners"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getOwnFollowingPartners(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)
        mp(RequestPartnersFollowed(eucc)).onSuccess {
            case RequestPartnersFollowedResponse(_, Right(fp)) => result.setResult(fp)
        }
        result
    }

    @RequestMapping(value = Array("/following"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getOwnFollowingUsers(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)

        mp(RequestUsersFollowed(eucc)).onSuccess {
            case RequestUsersFollowedResponse(_, Right(fus)) => result.setResult(fus)
        }
        result
    }

    @RequestMapping(value = Array("/followers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getOwnFollowers(eucc: EchoedUserClientCredentials) = {
        val result = new DeferredResult[Feed[UserContext]](null, ErrorResult.timeout)

        mp(RequestFollowers(eucc)).onSuccess {
            case RequestFollowersResponse(_, Right(fbu)) => result.setResult(fbu)
        }

        result
    }

}