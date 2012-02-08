package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.facebook.{FacebookService, FacebookServiceLocator}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}
import com.echoed.chamber.services.echo.EchoService
import org.springframework.web.servlet.ModelAndView
import scala.collection.JavaConversions
import com.echoed.chamber.services.adminuser._
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod,ResponseBody,PathVariable}
import com.echoed.chamber.controllers.CookieManager

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/7/12
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(Array("/admin"))
class AdminController {
    
    private val logger = LoggerFactory.getLogger(classOf[AdminController])
    
    @BeanProperty var adminUserServiceLocator: AdminUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _
    
    @RequestMapping(value = Array("/users"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getUsersJSON(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        
        if(continuation.isExpired){
            logger.error("Request expired getting users via admin api")
            continuation.setAttribute("jsonResponse","error")
            continuation.resume()
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

            adminUserServiceLocator.locateAdminUserService(adminUserId.get).onResult({
                case LocateAdminUserServiceResponse(_, Left(e)) => 
                    continuation.setAttribute("jsonResponse","error")
                    continuation.resume()
                case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                    logger.debug("User Ser")
                    adminUserService.getUsers.onResult({
                        case GetUsersResponse(_, Left(e)) => 
                            continuation.setAttribute("jsonResponse", "error")
                            continuation.resume()
                        case GetUsersResponse(_, Right(echoedUsers)) =>
                            logger.debug("Received Json Response For Users: {}",echoedUsers)
                            continuation.setAttribute("jsonResponse",echoedUsers)
                            continuation.resume()
                    })
            })
            continuation.undispatch()
        })

    }

}
