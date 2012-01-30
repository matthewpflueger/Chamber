package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import java.util.ArrayList
//import com.echoed.chamber.domain.EchoPossibility
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
//import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.echoeduser._
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._


@Controller
@RequestMapping(Array("/"))
class ExhibitController {

    private final val logger = LoggerFactory.getLogger(classOf[ExhibitController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var closetViewFacebook: String = _
    @BeanProperty var closetView: String = _


    @RequestMapping(method = Array(RequestMethod.GET))
    def exhibit(
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            @RequestParam(value="app", required = false) appType: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        def error(e: Option[Throwable]) = {
            e.foreach(logger.error("Error serving index page", _))
            new ModelAndView("view.index")
        }

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            error(Some(RequestExpiredException("Request expired to view exhibit for user %s" format echoedUserId)))
        } else if (echoedUserId == null) {
            error(None)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)


            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onComplete(_.value.get.fold(
                e => error(Some(e)),
                _ match {
                    case LocateWithIdResponse(_, Left(e)) => error(Some(e))
                    case LocateWithIdResponse(_, Right(echoedUserService)) =>
                        logger.debug("Found EchoedUserService {} ", echoedUserService);

                        echoedUserService.getCloset.onComplete(_.value.get.fold(
                            e => error(Some(e)),
                            _ match {
                                case GetExhibitResponse(_, Left(e)) => error(Some(e))
                                case GetExhibitResponse(_, Right(closet)) =>
                                    val view = if (appType == "facebook") closetViewFacebook else closetView;
                                    logger.debug("View for app type {}: {}", appType, view)

                                    val modelAndView = new ModelAndView(view)
                                    modelAndView.addObject("echoedUser", closet.echoedUser)
                                    modelAndView.addObject("totalCredit", "%.2f\n".format(closet.totalCredit))

                                    val error = Option(httpServletRequest.getParameter("error"))
                                    logger.debug("Found error: {}", error)
                                    modelAndView.addObject("errors", error.cata(
                                        e => Array[String](e),
                                        Array[String]()))

                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume()
                            }))
                }))

            continuation.undispatch()

        })
    }
}
