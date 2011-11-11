package com.echoed.chamber.controllers

import com.echoed.chamber.domain.EchoPossibility
import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.EchoService
import org.springframework.web.bind.annotation.{CookieValue, RequestMapping, RequestMethod}
import org.eclipse.jetty.continuation.ContinuationSupport
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping(Array("/echo"))
class EchoController {

    private val logger = LoggerFactory.getLogger(classOf[EchoController])

    @BeanProperty var buttonView: String = null
    @BeanProperty var loginView: String = null
    @BeanProperty var confirmView: String = null

    @BeanProperty var echoService: EchoService = null
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = null

    @RequestMapping(value = Array("/button"), method = Array(RequestMethod.GET))
    def button(
            //TODO cookies should be encrypted
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            echoPossibility: EchoPossibility) = {
        echoPossibility.echoedUserId = echoedUserId
        echoPossibility.step = "button" //TODO externalize this...
        recordEchoPossibility(echoPossibility)
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
        if (continuation.isExpired) {
            logger.error("Request expired to echo {}", echoPossibility)
            new ModelAndView(loginView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)

            val futureEchoedUserService = echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId)

            recordEchoPossibility(echoPossibility)

            futureEchoedUserService
                    .onResult {
                        case s: EchoedUserService =>
                            logger.debug("Found EchoedUser with id {}", echoedUserId)
                            echoPossibility.step = "confirm"
                            recordEchoPossibility(echoPossibility)
                            continuation.setAttribute("modelAndView", new ModelAndView(confirmView))
                            continuation.resume
                        case e =>
                            logger.error("Unexpected result {}", e)
                            continuation.setAttribute("modelAndView", new ModelAndView(loginView))
                            continuation.resume
    //                        new ModelAndView(loginView)
                    }
                    .onException {
                        case e =>
                            logger.error("Failed to find EchoedUser with id {} due to {}", echoedUserId, e)
                            continuation.setAttribute("modelAndView", new ModelAndView(loginView))
                            continuation.resume
    //                        new ModelAndView(loginView)
                    }
                    .onTimeout(
                        _ => {
                            logger.error("Timeout trying to find EchoedUser with id {}", echoedUserId)
                            continuation.setAttribute("modelAndView", new ModelAndView(loginView))
                            continuation.resume
    //                        new ModelAndView(loginView)
                        }
                    )

            continuation.undispatch
        })
//        }
    }

    def recordEchoPossibility(echoPossibility: EchoPossibility) {
        echoService.recordEchoPossibility(echoPossibility)
                .onComplete(_.value.get.fold(e => logger.error("Failed to record {} due to {}", echoPossibility, e),
                                             p => logger.debug("Recorded {}", p)))
                .onTimeout(_ => logger.error("Timeout recording {}", echoPossibility))
    }

}