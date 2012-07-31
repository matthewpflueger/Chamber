package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.context.request.async.DeferredResult
import org.slf4j.LoggerFactory

@Controller
@RequestMapping(Array("/posterror"))
class PostErrorController {

    private final val logger = LoggerFactory.getLogger(classOf[PostErrorController])

    @RequestMapping(method = Array(RequestMethod.POST))
    @ResponseBody
    def postError(
        @RequestParam(value = "error") error: String,
        @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
        @RequestHeader(value = "User-Agent", required = false) userAgent: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {

        logger.debug("Error Message: %s, IP: %s, User Agent: %s".format(error, remoteIp, userAgent))
        "Success"
    }

}
