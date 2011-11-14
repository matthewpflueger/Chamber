package com.echoed.chamber.controllers

import com.echoed.chamber.domain.EchoPossibility
import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.EchoService
import org.springframework.web.bind.annotation.{CookieValue, RequestMapping, RequestMethod}
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.util.CookieManager


@Controller
@RequestMapping(Array("/echo"))
class EchoController {

    private final val logger = LoggerFactory.getLogger(classOf[EchoController])

    @BeanProperty var buttonView: String = _
    @BeanProperty var loginView: String = _
    @BeanProperty var confirmView: String = _

    @BeanProperty var echoService: EchoService = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _

    @RequestMapping(value = Array("/button"), method = Array(RequestMethod.GET))
    def button(
            //TODO cookies should be encrypted
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            echoPossibility: EchoPossibility,
            httpServletResponse: HttpServletResponse) = {
        echoPossibility.echoedUserId = echoedUserId
        echoPossibility.step = "button" //TODO externalize this...
        recordEchoPossibility(echoPossibility, httpServletResponse)
        new ModelAndView(buttonView)
    }

    @RequestMapping(method = Array(RequestMethod.GET))
    def echo(
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            echoPossibility: EchoPossibility,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        echoPossibility.echoedUserId = echoedUserId
        echoPossibility.step = "login"

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (Option(echoPossibility.echoedUserId) == None) {
            logger.debug("Unknown user trying to echo {}", echoPossibility)
            recordEchoPossibility(echoPossibility, httpServletResponse)
            new ModelAndView(loginView)
        } else if (continuation.isExpired) {
            logger.error("Request expired to echo {}", echoPossibility)
            new ModelAndView(loginView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)

            recordEchoPossibility(echoPossibility, httpServletResponse)

            val futureEchoedUserService = echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId)
            futureEchoedUserService
                    .onResult {
                        case s: EchoedUserService =>
                            logger.debug("Found EchoedUser with id {}", echoedUserId)
                            echoPossibility.step = "confirm"
                            recordEchoPossibility(echoPossibility, httpServletResponse)

                            val modelAndView = new ModelAndView(confirmView)
                            modelAndView.addObject("echoPossibility", echoPossibility)
                            modelAndView.addObject("echoedUser", s.echoedUser.get)

                            continuation.setAttribute("modelAndView", modelAndView)
                            continuation.resume
                        case e =>
                            logger.error("Unexpected result {}", e)
                            continuation.setAttribute("modelAndView", new ModelAndView(loginView))
                            continuation.resume
                    }
                    .onException {
                        case e =>
                            logger.error("Failed to find EchoedUser with id {} due to {}", echoedUserId, e)
                            continuation.setAttribute("modelAndView", new ModelAndView(loginView))
                            continuation.resume
                    }
                    .onTimeout(
                        _ => {
                            logger.error("Timeout trying to find EchoedUser with id {}", echoedUserId)
                            continuation.setAttribute("modelAndView", new ModelAndView(loginView))
                            continuation.resume
                        }
                    )

            continuation.undispatch
        })
    }

    def recordEchoPossibility(echoPossibility: EchoPossibility, httpServletResponse: HttpServletResponse) {
        echoService.recordEchoPossibility(echoPossibility)
                .onComplete(_.value.get.fold(e => logger.error("Failed to record {} due to {}", echoPossibility, e),
                                             p => logger.debug("Recorded {}", p)))
                .onTimeout(_ => logger.error("Timeout recording {}", echoPossibility))

        httpServletResponse.addCookie(cookieManager.createCookie("echoPossibility", echoPossibility.id))
    }

}