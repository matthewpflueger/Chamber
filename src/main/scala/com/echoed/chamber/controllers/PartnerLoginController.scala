package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod,ResponseBody,PathVariable}


@Controller
class PartnerLoginController {

    private val logger = LoggerFactory.getLogger(classOf[PartnerLoginController])

    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var partnerLoginErrorView: String = _
    @BeanProperty var partnerLoginView: String = _


    @RequestMapping(value = Array("/partner/login"))
    def login(
            @RequestParam(value="email", required=false) email: String,
            @RequestParam(value="password", required= false) password: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {


        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to login {}", email)
            new ModelAndView(partnerLoginErrorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            if(email != null && password != null) {
                def onError(error: PartnerUserException) {
                    logger.debug("Got error during login for {}: {}", email, password)
                    continuation.setAttribute(
                        "modelAndView",
                        new ModelAndView(partnerLoginErrorView, "error", error))
                    continuation.resume()
                }

                logger.debug("Received login request for {}", email)

                partnerUserServiceLocator.login(email, password).onResult {
                    case LoginResponse(_, Left(error)) => onError(error)
                    case LoginResponse(_, Right(pus)) => pus.getPartnerUser.onResult {
                        case GetPartnerUserResponse(_, Left(error)) => onError(error)
                        case GetPartnerUserResponse(_, Right(pu)) =>
                            logger.debug("Successful login for {}", email)
                            cookieManager.addPartnerUserCookie(
                                    httpServletResponse,
                                    pu,
                                    httpServletRequest)
                            continuation.setAttribute("modelAndView", new ModelAndView(partnerLoginView))
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown response %s" format unknown)
                    }
                    case unknown => throw new RuntimeException("Unknown response %s" format unknown)
                }
            }
            else{
                continuation.setAttribute("modelAndView", new ModelAndView(partnerLoginErrorView))
                continuation.resume()
            }
            continuation.undispatch()
        })

    }

}
