package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import com.echoed.chamber.services.echoeduser._
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.event.EventService


@Controller
@RequestMapping(Array("/"))
class ExhibitController {

    private final val logger = LoggerFactory.getLogger(classOf[ExhibitController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var closetViewFacebook: String = _
    @BeanProperty var closetView: String = _
    @BeanProperty var indexView: String = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var eventService: EventService = _


    @RequestMapping(method = Array(RequestMethod.GET))
    def exhibit(
            @RequestParam(value="app", required = false) appType: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Option[Throwable] = None) = {
            e.foreach(logger.error("Error serving index page", _))
            val modelAndView = new ModelAndView(indexView)
            if (continuation.isSuspended) {
                continuation.setAttribute("modelAndView", modelAndView)
                continuation.resume
            }
            modelAndView
        }

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest)


        if (continuation.isExpired) {
            error(Some(RequestExpiredException("Request expired to view exhibit for user %s" format echoedUserId)))
        } else if (echoedUserId.isEmpty) {
            error()
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId.get).onComplete(_.fold(
                e => error(Some(e)),
                _ match {
                    case LocateWithIdResponse(_, Left(EchoedUserNotFound(id, _))) =>
                        logger.debug("Did not find an EchoedUser for {}", id)
                        error()
                    case LocateWithIdResponse(_, Right(echoedUserService)) =>
                        logger.debug("Found EchoedUserService {} ", echoedUserService);

                        echoedUserService.getCloset.onComplete(_.fold(
                            e => error(Some(e)),
                            _ match {
                                case GetExhibitResponse(_, Left(e)) => error(Some(e))
                                case GetExhibitResponse(_, Right(closet)) =>
                                    val view =
                                        if (appType == "facebook") {
                                            eventService.facebookCanvasViewed(closet.echoedUser)
                                            closetViewFacebook
                                        } else {
                                            eventService.exhibitViewed(closet.echoedUser)
                                            closetView
                                        }
                                    logger.debug("View for app type {}: {}", appType, view)

                                    val modelAndView = new ModelAndView(view)
                                    modelAndView.addObject("echoedUser", closet.echoedUser)
                                    modelAndView.addObject("totalCredit", "%.2f\n".format(closet.totalCredit))
                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume()
                            }))
                    case LocateWithIdResponse(_, Left(e)) => error(Some(e))
                }))

            continuation.undispatch()
        }
    }
}
