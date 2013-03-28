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
import com.echoed.chamber.domain.views.content.{Content, ContentDescription, PhotoContent}
import com.echoed.chamber.services.partneruser._
import scala.beans.BeanProperty
import java.util.Date
import com.echoed.util.ScalaObjectMapper
import com.echoed.chamber.services.partner._
import com.echoed.chamber.services.partneruser.GetPartnerSettingsResponse
import com.echoed.chamber.services.echoeduser.FollowPartner
import com.echoed.chamber.services.echoeduser.UnFollowPartnerResponse
import com.echoed.chamber.services.echoeduser.UnFollowPartner
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import com.echoed.chamber.domain.Topic
import com.echoed.chamber.services.partneruser.UpdatePartnerCustomization
import com.echoed.chamber.domain.views.Feed
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.partneruser.GetPartnerSettings
import com.echoed.chamber.services.echoeduser.FollowPartnerResponse
import com.echoed.chamber.services.echoeduser.PartnerFollower
import com.echoed.chamber.domain.views.context.PartnerContext
import com.echoed.chamber.services.state.{QueryStoriesForPartnerResponse, QueryStoriesForPartner}
import com.echoed.chamber.services.partner.PutTopic
import com.echoed.chamber.services.partneruser.GetPartnerSettingsResponse
import com.echoed.chamber.services.echoeduser.FollowPartner
import com.echoed.chamber.services.partner.RequestPartnerContentResponse
import com.echoed.chamber.services.echoeduser.UnFollowPartnerResponse
import com.echoed.chamber.services.echoeduser.UnFollowPartner
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import com.echoed.chamber.services.partner.RequestTopics
import com.echoed.chamber.domain.Topic
import com.echoed.chamber.services.partneruser.UpdatePartnerCustomization
import com.echoed.chamber.services.state.QueryStoriesForPartner
import com.echoed.chamber.services.partner.RequestPartnerFollowers
import com.echoed.chamber.domain.StoryState
import views.content.ContentDescription
import views.context.PartnerContext
import views.Feed
import com.echoed.chamber.services.state.QueryStoriesForPartnerResponse
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.partneruser.GetPartnerSettings
import com.echoed.chamber.services.partner.PutTopicResponse
import com.echoed.chamber.services.partner.RequestPartnerContent
import com.echoed.chamber.services.partneruser.UpdatePartnerCustomizationResponse
import com.echoed.chamber.services.echoeduser.FollowPartnerResponse
import com.echoed.chamber.services.partner.PartnerClientCredentials
import com.echoed.chamber.services.echoeduser.PartnerFollower
import com.echoed.chamber.services.partner.RequestTopicsResponse
import com.echoed.chamber.services.partner.RequestPartnerFollowersResponse


@Controller
@RequestMapping(Array("/api/partner"))
class PartnerController extends EchoedController {

    @RequestMapping(value = Array("/{partnerId}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerContentStories(
            @PathVariable(value = "partnerId") partnerId: String,
            @RequestParam(value = "contentPath", required = false) contentPath: String,
            @RequestParam(value = "startsWith", required = false, defaultValue = "false") startsWith: Boolean,
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        getPartnerContent(null, partnerId, contentPath, startsWith, page, origin)

    }

    @RequestMapping(value = Array("/{partnerId}/{contentType}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerContentOther(
            @PathVariable(value = "partnerId") partnerId: String,
            @PathVariable(value = "contentType") contentType: String,
            @RequestParam(value = "contentPath", required = false) contentPath: String,
            @RequestParam(value = "startsWith", required = false, defaultValue = "false") startsWith: Boolean,
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "origin", required = false, defaultValue = "echoed") origin: String) = {

        getPartnerContent(Content.getContentDescription(contentType), partnerId, contentPath, startsWith, page, origin)
    }

    def getPartnerContent(
            contentType:    ContentDescription,
            partnerId:      String,
            contentPath:    String,
            startsWith:     Boolean,
            page:           Int,
            origin:         String) = {

        val result = new DeferredResult[Feed[PartnerContext]](null, ErrorResult.timeout)

        log.debug("Requesting for Partner Content for Partner {}", partnerId )

        mp(RequestPartnerContent(
                new PartnerClientCredentials(partnerId),
                origin,
                Option(contentType),
                Option(contentPath),
                Option(startsWith),
                Option(page))).onSuccess {
            case RequestPartnerContentResponse(_, Right(partnerFeed)) => result.setResult(partnerFeed)
        }
        result

    }

    @RequestMapping(value = Array("/{partnerId}/followers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerFollowers(@PathVariable(value = "partnerId") partnerId: String) = {

        val result = new DeferredResult[Feed[PartnerContext]](null, ErrorResult.timeout)

        mp(RequestPartnerFollowers(new PartnerClientCredentials(partnerId))).onSuccess {
            case RequestPartnerFollowersResponse(_, Right(partnerFeed)) => result.setResult(partnerFeed)
        }

        result
    }

    @RequestMapping(value = Array("/{partnerId}/followers"), method = Array(RequestMethod.PUT))
    @ResponseBody
    def putPartnerFollower(
            @PathVariable(value = "partnerId") partnerId: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[List[PartnerFollower]](null, ErrorResult.timeout)

        mp(FollowPartner(eucc, partnerId)).onSuccess {
            case FollowPartnerResponse(_, Right(fp)) => result.setResult(fp)
        }
        result
    }

    @RequestMapping(value = Array("/{partnerId}/followers"), method = Array(RequestMethod.DELETE))
    @ResponseBody
    def deletePartnerFollower(
            @PathVariable(value = "partnerId") partnerId: String,
            eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[List[PartnerFollower]](null, ErrorResult.timeout)

        mp(UnFollowPartner(eucc, partnerId)).onSuccess {
            case UnFollowPartnerResponse(_, Right(fp)) => result.setResult(fp)
        }
        result
    }

    @RequestMapping(value = Array("/{partnerId}/topics}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerTopics(@PathVariable(value = "id") partnerId: String) = {
        val result = new DeferredResult[List[Topic]](null, ErrorResult.timeout)

        mp(RequestTopics(new PartnerClientCredentials(partnerId))).onSuccess {
            case RequestTopicsResponse(_, Right(topics)) => result.setResult(topics)
        }
        result
    }


    @RequestMapping(
        value = Array("/topics"),
        method = Array(RequestMethod.POST),
        consumes = Array("application/json"))
    @ResponseBody
    def postTopic(
            @RequestBody topic: TopicParams,
            pucc: PartnerUserClientCredentials) = updateTopic(pucc, topic)


    @RequestMapping(
        value = Array("/topics/{id}"),
        method = Array(RequestMethod.PUT),
        consumes = Array("application/json"))
    @ResponseBody
    def putTopic(
            @PathVariable id: String,
            @RequestBody topic: TopicParams,
            pucc: PartnerUserClientCredentials) = updateTopic(pucc, topic, Option(id))

    private def updateTopic(
            pucc: PartnerUserClientCredentials,
            topic: TopicParams,
            id: Option[String] = None) = {

        val result = new DeferredResult[Topic](null, ErrorResult.timeout)

        mp(PutTopic(
                PartnerClientCredentials(pucc.partnerId.get),
                topic.title,
                Option(topic.description),
                Option(topic.beginOn),
                Option(topic.endOn),
                id.orElse(Option(topic.id)),
                Option(topic.community))).onSuccess {
            case PutTopicResponse(_, Right(t)) =>
                result.setResult(t)
        }
        result
    }

    @RequestMapping(value = Array("/settings/customization/*"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getCustomization(pucc: PartnerUserClientCredentials) = {
        val result = new DeferredResult[Map[String, Any]](null, ErrorResult.timeout)

        mp(GetPartnerSettings(pucc)).onSuccess {
            case GetPartnerSettingsResponse(_, Right(partnerSettings)) =>
                result.setResult(partnerSettings.headOption.map(_.makeCustomizationOptions).orNull)
        }

        result
    }

    @RequestMapping(
        value = Array("/settings/customization/*"),
        method = Array(RequestMethod.PUT),
        consumes = Array("application/json"))
    @ResponseBody
    def putCustomization(
            @RequestBody cParams: CustomizationParams,
            pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult[Map[String, Any]](null, ErrorResult.timeout)
        val params = new ScalaObjectMapper().convertValue(cParams, classOf[Map[String, Any]])

        mp(UpdatePartnerCustomization(
            pucc,
            params)).onSuccess {
            case UpdatePartnerCustomizationResponse(_, Right(customization)) =>
                result.setResult(customization)
        }
        result
    }

    @RequestMapping(value = Array("/stories"), method = Array(RequestMethod.GET))
    @ResponseBody
    def queryStories(
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "pageSize", required = false, defaultValue = "30") pageSize: Int,
            @RequestParam(value = "moderated", required = false) moderated: String,
            pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult[List[StoryState]](null, ErrorResult.timeout)

        mp(QueryStoriesForPartner(pucc, page, pageSize, Option(moderated).map(_.toBoolean))).onSuccess {
            case QueryStoriesForPartnerResponse(_, Right(stories)) =>
                result.setResult(stories)
        }

        result
    }

}


class CustomizationParams(
         @BeanProperty var useGallery: Boolean,
         @BeanProperty var showGallery: Boolean,
         @BeanProperty var useRemote: Boolean,
         @BeanProperty var remoteVertical: String,
         @BeanProperty var remoteHorizontal: String,
         @BeanProperty var remoteOrientation: String,
         @BeanProperty var widgetTitle: String,
         @BeanProperty var widgetShareMessage: String) {

    def this() = this(false, true, true, null, null, null, null, null)

}

class TopicParams(
        @BeanProperty var title: String,
        @BeanProperty var description: String,
        @BeanProperty var beginOn: Date,
        @BeanProperty var endOn: Date,
        @BeanProperty var id: String,
        @BeanProperty var community: String) {

    def this() = this(null, null, null, null, null, null)
}
