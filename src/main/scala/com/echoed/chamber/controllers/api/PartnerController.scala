package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod,ResponseBody,PathVariable}
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import org.springframework.web.context.request.async.DeferredResult


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

}
