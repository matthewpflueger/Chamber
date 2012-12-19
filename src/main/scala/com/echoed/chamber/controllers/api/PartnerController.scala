package com.echoed.chamber.controllers.api

import scala.reflect.BeanProperty
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.partneruser._
import scala.Right
import com.echoed.chamber.services.state.{QueryStoriesForPartnerResponse, QueryStoriesForPartner}
import com.echoed.chamber.controllers.interceptors.Secure
import com.echoed.chamber.services.partneruser.UpdatePartnerCustomization
import com.echoed.chamber.services.state.QueryStoriesForPartner
import com.echoed.chamber.services.state.QueryStoriesForPartnerResponse
import com.echoed.chamber.services.partneruser.GetPartnerSettingsResponse
import com.echoed.chamber.services.partneruser.GetPartnerSettings
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import scala.Right
import com.echoed.chamber.services.partner.{GetTopicsResponse, GetTopics, PutTopicResponse, PartnerClientCredentials, PutTopic}
import java.util.Date
import com.fasterxml.jackson.annotation.JsonFormat
import com.echoed.chamber.services.topic.ReadTopics
import com.echoed.util.ScalaObjectMapper


@Controller
@Secure
@RequestMapping(Array("/partner"))
class PartnerController extends EchoedController {

    @RequestMapping(value = Array("/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getSettings(pucc: PartnerUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetPartnerSettings(pucc)).onSuccess {
            case GetPartnerSettingsResponse(_, Right(partnerSettings)) =>
                log.debug("Successfully received partnerSettings for {}", pucc)
                result.set(partnerSettings)
        }

        result
    }

    @RequestMapping(value = Array("/settings/topics"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getTopics(pucc: PartnerUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetTopics(PartnerClientCredentials(pucc.partnerId.get))).onSuccess {
            case GetTopicsResponse(_, Right(topics)) => result.set(topics)
        }

        result
    }

    @RequestMapping(
            value = Array("/settings/topics"),
            method = Array(RequestMethod.POST),
            consumes = Array("application/json"))
    @ResponseBody
    def postTopic(
            @RequestBody topic: TopicParams,
            pucc: PartnerUserClientCredentials) = updateTopic(pucc, topic)


    @RequestMapping(
        value = Array("/settings/topics/{id}"),
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
        val result = new DeferredResult(ErrorResult.timeout)
        mp(PutTopic(
                PartnerClientCredentials(pucc.partnerId.get),
                topic.title,
                Option(topic.description),
                Option(topic.beginOn),
                Option(topic.endOn),
                id.orElse(Option(topic.id)),
                Option(topic.community))).onSuccess {
            case PutTopicResponse(_, Right(topic)) =>
                result.set(topic)
        }
        result
    }

    @RequestMapping(value = Array("/settings/customization/*"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getCustomization(pucc: PartnerUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)
        mp(GetPartnerSettings(pucc)).onSuccess {
            case GetPartnerSettingsResponse(_, Right(partnerSettings)) =>
                result.set(partnerSettings.headOption.map(_.makeCustomizationOptions).orNull)
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

        val result = new DeferredResult(ErrorResult.timeout)
        val params = new ScalaObjectMapper().convertValue(cParams, classOf[Map[String, Any]])
        mp(UpdatePartnerCustomization(
                pucc,
                params)).onSuccess {
            case UpdatePartnerCustomizationResponse(_, Right(customization)) =>
                result.set(customization)
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

        val result = new DeferredResult(ErrorResult.timeout)

        mp(QueryStoriesForPartner(pucc, page, pageSize, Option(moderated).map(_.toBoolean))).onSuccess {
            case QueryStoriesForPartnerResponse(_, Right(stories)) =>
                result.set(stories)
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

