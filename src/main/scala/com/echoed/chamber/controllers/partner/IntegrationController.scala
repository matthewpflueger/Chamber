package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller

import scala.reflect.BeanProperty

import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import com.echoed.chamber.controllers.{RequestExpiredException, Errors, CookieManager}
import com.echoed.chamber.services.partneruser.{GetPartnerUserResponse, LocateResponse, PartnerUserServiceLocator}


@Controller
class IntegrationController {

    private val logger = LoggerFactory.getLogger(classOf[IntegrationController])

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var integrationView: String = _
    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @RequestMapping(value = Array("/partner/integration"), method = Array(RequestMethod.GET))
    def integration(request: HttpServletRequest, response: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(request)

        def error(e: Option[Throwable] = None) = {
            e.foreach(ex => logger.error("%s" format ex.getMessage, ex))
            val modelAndView = new ModelAndView(integrationView) with Errors
            modelAndView.addError(e)
            continuation.setAttribute("modelAndView", modelAndView)
            if (continuation.isSuspended) continuation.resume
            modelAndView
        }

        if (continuation.isExpired) {
            error(Some(RequestExpiredException()))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(response)

            val partnerUserId = cookieManager.findPartnerUserCookie(request)

            logger.debug("Showing integration pages for PartnerUser {}", partnerUserId)

            partnerUserServiceLocator.locate(partnerUserId.get).onComplete(_.value.get.fold(
                e => error(Some(e)),
                _ match {
                    case LocateResponse(_, Left(e)) => error(Some(e))
                    case LocateResponse(_, Right(partnerUserService)) => partnerUserService.getPartnerUser.onComplete(_.value.get.fold(
                        e => error(Some(e)),
                        _ match {
                            case GetPartnerUserResponse(_, Left(e)) => error(Some(e))
                            case GetPartnerUserResponse(_, Right(partnerUser)) =>
                                val modelAndView = new ModelAndView(integrationView)
                                modelAndView.addObject("partnerUser", partnerUser)
                                continuation.setAttribute("modelAndView", modelAndView)
                                continuation.resume
                        }))
                }))


            continuation.undispatch()
        })
    }

}
