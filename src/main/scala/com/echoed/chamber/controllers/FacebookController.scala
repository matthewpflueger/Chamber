package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.facebook.{FacebookService, FacebookServiceLocator}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}
import com.echoed.util.CookieManager
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import com.echoed.chamber.services.EchoService
import akka.util.BoxedType._
import scalaz._
import Scalaz._
import com.echoed.chamber.domain.{EchoedUser, EchoPossibility}
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping(Array("/facebook"))
class FacebookController {

    private val logger = LoggerFactory.getLogger(classOf[FacebookController])

    @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoService: EchoService = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var facebookLoginErrorView: String = _
    @BeanProperty var echoView: String = _
    @BeanProperty var dashboardView: String = _


    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam("code") code: String,
            @CookieValue(value = "echoPossibility", required = false) echoPossibilityId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (Option(code) == None || continuation.isExpired) {
            logger.error("Request expired to login via Facebook with code {}", code)
            new ModelAndView(facebookLoginErrorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)

            logger.debug("Requesting EchoPossibility with id {}", echoPossibilityId)
            val futureEchoPossibility = echoService.getEchoPossibility(echoPossibilityId)

            logger.debug("Requesting FacebookService with code {}", code)
            val futureFacebookService = facebookServiceLocator.getFacebookServiceWithCode(code)

            futureFacebookService
                    .onResult {
                        case facebookService: FacebookService =>
                            logger.debug("Received FacebookService with code {}", code)
                            logger.debug("Requesting EchoedUserService with FacebookService with code {}", code)
                            val futureEchoedUserService = echoedUserServiceLocator.getEchoedUserServiceWithFacebookService(facebookService)
                            futureEchoedUserService
                                    .onResult {
                                        case s: EchoedUserService =>
                                            logger.debug("Received EchoedUserService using FacebookService with code {}", code)
                                            continuation.setAttribute("modelAndView",
                                                try {
                                                    val echoedUser = s.echoedUser.get
                                                    cookieManager.addCookie(httpServletResponse, "echoedUserId", echoedUser.id)
                                                    val echoPossibility = futureEchoPossibility.get

                                                    val modelAndView = new ModelAndView(echoView)
                                                    modelAndView.addObject("echoPossibility", futureEchoPossibility.get)
                                                    modelAndView.addObject("echoedUser", echoedUser)
                                                } catch {
                                                    case n: NoSuchElementException =>
                                                        logger.error("Error getting EchoedUser {}", n)
                                                        new ModelAndView(facebookLoginErrorView)
                                                    case e =>
                                                        logger.debug("No EchoPossibility with id {}", echoPossibilityId)
                                                        new ModelAndView(dashboardView)
                                                })
                                            continuation.resume
                                        case e =>
                                            logger.error("Unexpected result {}", e)
                                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                            continuation.resume
                                    }
                                    .onException {
                                        case e =>
                                            logger.error("Failed to receive EchoedUserService with code {} due to {}", code, e)
                                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                            continuation.resume
                                    }
                                    .onTimeout(
                                        _ => {
                                            logger.error("Timeout requesting EchoedUserService with code {}", code)
                                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                            continuation.resume
                                        }
                                    )
                        case e =>
                            logger.error("Unexpected result {}", e)
                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                            continuation.resume
                    }
                    .onException {
                        case e =>
                            logger.error("Failed to receive FacebookService with code {} due to {}", code, e)
                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                            continuation.resume
                    }
                    .onTimeout(
                        _ => {
                            logger.error("Timeout requesting FacebookService with code {}", code)
                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                            continuation.resume
                        }
                    )

            continuation.undispatch
        })

    }

}

