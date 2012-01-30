package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.util.CookieManager
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}


@Controller
class PartnerDashboardController {

    private val logger = LoggerFactory.getLogger(classOf[PartnerDashboardController])

    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @BeanProperty var partnerDashboardErrorView: String = _
    @BeanProperty var partnerDashboardView: String = _


    @RequestMapping(value = Array("/partner/dashboard"), method = Array(RequestMethod.GET))
    def dashboard(
            @CookieValue(value = "partnerUser", required = false) partnerUserId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view dashboard for {}", partnerUserId)
            new ModelAndView(partnerDashboardErrorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            def onError(error: PartnerUserException) {
                logger.debug("Got error showing dashboard for {}: {}", partnerUserId, error.message)
                continuation.setAttribute(
                    "modelAndView",
                    new ModelAndView(partnerDashboardErrorView, "error", error))
                continuation.resume()
            }

            logger.debug("Showing dashboard for PartnerUser {}", partnerUserId)
            partnerUserServiceLocator.locate(partnerUserId).onResult {
                case LocateResponse(_, Left(error)) => onError(error)
                case LocateResponse(_, Right(pus)) => pus.getPartnerUser.onResult {
                    case GetPartnerUserResponse(_, Left(error)) => onError(error)
                    case GetPartnerUserResponse(_, Right(pu)) =>
                        logger.debug("Got {}", pu)
                        continuation.setAttribute(
                            "modelAndView",
                            new ModelAndView(partnerDashboardView, "partnerUser", pu))
                        continuation.resume()
                    case unknown => throw new RuntimeException("Unknown response %s" format unknown)
                }
                case unknown => throw new RuntimeException("Unknown response %s" format unknown)
            }

            continuation.undispatch()
        })

    }

}
