package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.ContinuationSupport
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.adminuser._

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/7/12
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
class AdminDashboardController {


    @BeanProperty var adminUserServiceLocator: AdminUserServiceLocator = _

    @BeanProperty var adminLoginView: String = _
    @BeanProperty var adminDashboardView: String = _

    @BeanProperty var cookieManager: CookieManager = _

    private final val logger = LoggerFactory.getLogger(classOf[AdminLoginController])

    @RequestMapping(Array("/admin/dashboard"))
    def login(   httpServletRequest: HttpServletRequest,
                 httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(continuation.isExpired){

        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            if(adminUserId != null){

                def onError(error: AdminUserException) {
                    continuation.setAttribute("modelAndView","error")
                    continuation.resume()
                }
                
                adminUserServiceLocator.locateAdminUserService(adminUserId).onResult({
                    case LocateAdminUserServiceResponse(_, Left(error)) => onError(error)
                    case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                            continuation.setAttribute("modelAndView",adminDashboardView)
                            continuation.resume()
                })
            } else{
                continuation.setAttribute("modelAndView","redirect: http://www.echoed.com/admin/login")
                continuation.resume()
            }
            continuation.undispatch()
        })
    }
}
