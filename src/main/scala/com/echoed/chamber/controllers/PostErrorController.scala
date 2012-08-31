package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._

@Controller
@RequestMapping(Array("/posterror"))
class PostErrorController extends EchoedController {

    @RequestMapping(method = Array(RequestMethod.POST))
    @ResponseBody
    def postError(
            @RequestParam(value = "error") error: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            @RequestHeader(value = "User-Agent", required = false) userAgent: String) = {

        log.debug("Error Message: {}, IP: {}, User Agent: {}", error, remoteIp, userAgent)
        "Success"
    }

}
