package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.EchoService
import org.springframework.web.bind.annotation.{CookieValue, RequestMapping, RequestMethod}
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.util.CookieManager
import akka.dispatch.Future
import scalaz._
import Scalaz._
import com.echoed.chamber.domain.{EchoedUser, EchoPossibility}
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping(Array("/echo"))
class EchoController {

    private final val logger = LoggerFactory.getLogger(classOf[EchoController])

    @BeanProperty var buttonView: String = _
    @BeanProperty var loginView: String = _
    @BeanProperty var confirmView: String = _
    @BeanProperty var errorView: String = _
    @BeanProperty var echoConfirm: String = _

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
            @CookieValue(value = "echoPossibility", required = false) echoPossibilityId: String,
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

            val futureEchoPossibility = echoService.getEchoPossibility(echoPossibilityId)
            val futureEchoedUser = echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId)

            Future {
                try {
                    val modelAndView = new ModelAndView(confirmView)
                    val echoedUser = futureEchoedUser.get.echoedUser.get
                    modelAndView.addObject("echoedUser", echoedUser)

                    val confirmEchoPossibility = try { futureEchoPossibility.get } catch { case e => echoPossibility }
                    modelAndView.addObject("echoPossibility", confirmEchoPossibility)
                    continuation.setAttribute("modelAndView", modelAndView)

                    confirmEchoPossibility.step = "confirm"
                    confirmEchoPossibility.echoedUserId = echoedUser.id
                    recordEchoPossibility(confirmEchoPossibility, httpServletResponse)
                } catch {
                    case e =>
                        logger.debug(
                            "Failed echo confirmation for {} with {} due to {}",
                            Array(echoedUserId, echoPossibility, e))

                        recordEchoPossibility(echoPossibility, httpServletResponse)

                        val modelAndView = new ModelAndView(loginView)
                        modelAndView.addObject("echoPossibility", echoPossibility)

                        continuation.setAttribute("modelAndView", modelAndView)
                } finally {
                    continuation.resume()
                }
            }

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
