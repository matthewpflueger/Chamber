package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import com.echoed.chamber.services.echoeduser._
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.event.EventService
import org.springframework.web.context.request.async.DeferredResult


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

        val errorModelAndView = new ModelAndView(indexView)
        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest)

        if (echoedUserId.isEmpty) {
            errorModelAndView
        } else {
            val result = new DeferredResult(errorModelAndView)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId.get).onComplete(_.fold(
                e => result.set(errorModelAndView),
                _ match {
                    case LocateWithIdResponse(_, Left(EchoedUserNotFound(id, _))) =>
                        logger.debug("Did not find an EchoedUser for {}", id)
                        result.set(errorModelAndView)
                    case LocateWithIdResponse(_, Right(echoedUserService)) =>
                        logger.debug("Found EchoedUserService {} ", echoedUserService);

                        echoedUserService.getCloset.onComplete(_.fold(
                            e => result.set(errorModelAndView),
                            _ match {
                                case GetExhibitResponse(_, Left(e)) => result.set(errorModelAndView)
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
                                    result.set(modelAndView)
                            }))
                }))

            result
        }

    }
}
