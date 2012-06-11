package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.chamber.services.echo.{EchoService, GetEchoByIdResponse}
import org.springframework.web.context.request.async.DeferredResult

@Controller
@RequestMapping(Array("/graph"))
class FacebookGraphController {

    @BeanProperty var echoService: EchoService = _
    @BeanProperty var facebookClientId: String = _
    @BeanProperty var facebookAppNameSpace: String = _
    @BeanProperty var errorView: String = _

    @BeanProperty var facebookGraphProductView: String = _

    private final val logger = LoggerFactory.getLogger(classOf[FacebookGraphController])

    @RequestMapping(value = Array("/product/{linkId}"), method = Array(RequestMethod.GET))
    def product(
                 @PathVariable(value = "linkId") linkId: String,
                 httpServletRequest: HttpServletRequest,
                 httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        logger.debug("Retrieving Facebook Graph Product Page for Echo: {}", linkId)
        echoService.getEchoById(linkId).onSuccess {
            case GetEchoByIdResponse(msg, Right(echo)) =>
                logger.debug("Successfully retreived Echo {} , Responding with Facebook Graph Product View", echo)
                val modelAndView = new ModelAndView(facebookGraphProductView)
                modelAndView.addObject("echo", echo)
                modelAndView.addObject("facebookClientId", facebookClientId)
                modelAndView.addObject("facebookAppNameSpace", facebookAppNameSpace)
                result.set(modelAndView)
        }

        result
    }
}
