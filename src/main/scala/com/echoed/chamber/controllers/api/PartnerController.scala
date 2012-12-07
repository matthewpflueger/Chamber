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

    @RequestMapping(value = Array("/settings/customization"), method = Array(RequestMethod.GET))
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
        value = Array("/settings/customization"),
        method = Array(RequestMethod.PUT),
        consumes = Array("application/json"))
    @ResponseBody
    def putCustomization(
        @RequestBody cParams: CustomizationParams,
        pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)
        mp(UpdatePartnerCustomization(
            pucc,
            cParams.useGallery,
            cParams.useRemote,
            cParams.remoteVertical,
            cParams.remoteHorizontal,
            cParams.remoteOrientation)).onSuccess {
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
            @BeanProperty var useRemote: Boolean,
            @BeanProperty var remoteVertical: String,
            @BeanProperty var remoteHorizontal: String,
            @BeanProperty var remoteOrientation: String) {

    def this() = this(false, true, null, null, null)

}