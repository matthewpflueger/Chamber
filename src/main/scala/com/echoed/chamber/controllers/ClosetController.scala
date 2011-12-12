package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import com.echoed.chamber.domain.EchoPossibility
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping(Array("/closet"))
class ClosetController {

    private final val logger = LoggerFactory.getLogger(classOf[ClosetController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var closetView: String = _
    @BeanProperty var errorView: String = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def closet(
            @CookieValue(value = "echoedUserId", required = true) echoedUserId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view closet for user {}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).map { echoedUserService =>
                echoedUserService.getCloset.map { closet =>
                        val modelAndView = new ModelAndView(closetView)
                        modelAndView.addObject("echoedUser", closet.echoedUser)
                        modelAndView.addObject("echoes", closet.echoes)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()
                }
            }

            continuation.undispatch()
        })
    }

    @RequestMapping(value = Array("/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def feed(
            @CookieValue(value = "echoedUserId", required= false) echoedUserIdCookie:String,
            @RequestParam(value="echoedUserId", required = false) echoedUserIdParam:String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        var echoedUserId: String = null;
        if(echoedUserIdCookie != null){
            echoedUserId = echoedUserIdCookie;
        }
        else{
            echoedUserId = echoedUserIdParam;
        }

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired){
            logger.error("Request expired to view exhibit for user{}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("feed")).getOrElse({
            continuation.suspend(httpServletResponse)

            continuation.undispatch()
        })

    }

    @RequestMapping(value = Array("/exhibit"), method = Array(RequestMethod.GET))
    @ResponseBody
    def exhibit(
            @CookieValue(value = "echoedUserId", required = false) echoedUserIdCookie: String,
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {


        var echoedUserId:String = null;
        if(echoedUserIdCookie != null){
            echoedUserId = echoedUserIdCookie;
        }
        else{
            echoedUserId = echoedUserIdParam;
        }

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view exhibit for user {}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("exhibit")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).map { echoedUserService =>
                echoedUserService.getCloset.map { closet =>
                    continuation.setAttribute("exhibit", closet.echoes)
                    continuation.resume()
                }
            }

            continuation.undispatch()
        })
    }
}
