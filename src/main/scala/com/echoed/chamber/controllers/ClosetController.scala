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
@RequestMapping(Array("/closet"))
class ClosetController {

    private final val logger = LoggerFactory.getLogger(classOf[ClosetController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var closetViewFacebook: String = _
    @BeanProperty var closetView: String = _
    @BeanProperty var errorView: String = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def closet(
            @CookieValue(value = "echoedUserId", required = true) echoedUserId: String,
            @RequestParam(value="app", required = false) appType: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view closet for user {}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)
            var view = "";
            logger.debug("App Type: {} ", appType)

            if(appType != null){
                if(appType.equals("facebook")){
                    view = closetViewFacebook
                }
            }
            else{
                view = closetView
            }

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_,Left(error)) =>
                    logger.error("Error Locating EchoedUserService: {}" , error)
                    throw new RuntimeException("Unknown Response %s" format error)
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    logger.debug("Found EchoedUserService {} ", echoedUserService);

                    echoedUserService.getCloset.onResult{
                        case GetExhibitResponse(_,Left(error)) =>
                            logger.error("Error Getting Exhibit: {}" , error)
                            throw new RuntimeException("Unknown Response %s" format error)
                        case GetExhibitResponse(_,Right(closet)) =>
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
                        case unknown => throw new RuntimeException("Unknown Response %s" format unknown)
                    }
                    .onException{
                        case e=>
                            logger.error("Exception thrown on Getting Exhibit: {}", e)
                            val modelAndView  = new ModelAndView(errorView)
                            continuation.setAttribute("modelAndView",modelAndView)
                            continuation.resume()
                    }
            }
            .onException{
                case e =>
                    logger.error("Exception thrown Locating EchoedUserService: {}" , e)
                    val modelAndView = new ModelAndView(errorView)
                    continuation.setAttribute("modelAndView", modelAndView)
                    continuation.resume()
            }
            continuation.undispatch()
        })
    }
}
