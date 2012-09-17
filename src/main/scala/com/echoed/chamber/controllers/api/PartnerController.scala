package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.partneruser.GetPartnerSettings
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import scala.Right
import com.echoed.chamber.services.partneruser.GetPartnerSettingsResponse
import com.echoed.chamber.services.state.{QueryStoriesForPartnerResponse, QueryStoriesForPartner}


@Controller
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


    @RequestMapping(value = Array("/stories"), method = Array(RequestMethod.GET))
    @ResponseBody
    def queryStories(
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "pageSize", required = false, defaultValue = "30") pageSize: Int,
            @RequestParam(value = "moderated", required = false, defaultValue = "") moderated: String,
            pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        val mod = moderated match {
            case "" =>
                None
            case "true" =>
                Option(true)
            case "false" =>
                Option(false)
        }

        mp(QueryStoriesForPartner(pucc, page, pageSize, mod)).onSuccess {
            case QueryStoriesForPartnerResponse(_, Right(stories)) =>
                result.set(stories)
        }

        result
    }


}
