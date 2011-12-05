package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{CookieValue, RequestMethod, RequestMapping}
import com.echoed.chamber.domain.EchoPossibility
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import org.eclipse.jetty.continuation.ContinuationSupport
import org.springframework.web.servlet.ModelAndView
import org.slf4j.LoggerFactory


@Controller
@RequestMapping(Array("/closet"))
class ClosetController {

    private final val logger = LoggerFactory.getLogger(classOf[ClosetController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var closetView: String = _
    @BeanProperty var errorView: String = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def closet(
            @CookieValue(value = "echoedUserId", required = true) echoedUserId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view closet for user {}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).map { echoedUserService =>
                echoedUserService.getCloset.map { closet =>
                        val modelAndView = new ModelAndView(closetView)
                        modelAndView.addObject("echoedUser", closet.echoedUser)
                        modelAndView.addObject("echoes", closet.echoes)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()
                }
            }

            continuation.undispatch()
        })


    }
}
