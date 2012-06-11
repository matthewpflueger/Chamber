package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.continuation.{Continuation, ContinuationSupport}
import com.echoed.chamber.controllers.ControllerUtils._
import com.echoed.chamber.services.echo.{EchoService, GetEchoByIdResponse}

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

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if(continuation.isExpired) {
            error(errorView, Some(RequestExpiredException()))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            logger.debug("Retrieving Facebook Graph Product Page for Echo: {}", linkId)
            echoService.getEchoById(linkId).onComplete(_.fold(
                e => error(errorView, Some(e)),
                _ match {
                    case GetEchoByIdResponse(msg, Left(e)) => error(errorView, Some(e))
                    case GetEchoByIdResponse(msg, Right(echo)) =>
                        logger.debug("Successfully retreived Echo {} , Responding with Facebook Graph Product View", echo)
                        val modelAndView = new ModelAndView(facebookGraphProductView)
                        modelAndView.addObject("echo", echo)
                        modelAndView.addObject("facebookClientId", facebookClientId)
                        modelAndView.addObject("facebookAppNameSpace", facebookAppNameSpace)
                        continuation.setAttribute("modelAndView",modelAndView)
                        continuation.resume()
                }))

            continuation.undispatch()
        })
    }
}
