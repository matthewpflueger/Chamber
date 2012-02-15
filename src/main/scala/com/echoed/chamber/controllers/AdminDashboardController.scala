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
    @BeanProperty var adminDashboardErrorView: String = _

    @BeanProperty var cookieManager: CookieManager = _

    private final val logger = LoggerFactory.getLogger(classOf[AdminLoginController])

    @RequestMapping(Array("/admin/dashboard"))
    def dashboard(
                httpServletRequest: HttpServletRequest,
                httpServletResponse: HttpServletResponse) = {

        val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

        logger.debug("Admin User: {}", adminUserId)
        
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(continuation.isExpired){
            new ModelAndView(adminDashboardErrorView)
        } else if (adminUserId.isEmpty) {
            new ModelAndView(adminDashboardErrorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            def onError(error: AdminUserException){
                continuation.setAttribute(
                    "modelAndView",
                    new ModelAndView(adminDashboardErrorView))
                continuation.resume()
            }

            adminUserServiceLocator.locateAdminUserService(adminUserId.get).onResult {
                    case LocateAdminUserServiceResponse(_, Left(error)) => onError(error)
                    case LocateAdminUserServiceResponse(_, Right(adminUserService)) => adminUserService.getAdminUser.onResult {
                        case GetAdminUserResponse(_, Left(error)) => onError(error)
                        case GetAdminUserResponse(_, Right(au)) =>
                            logger.debug("Got {}", au)
                            continuation.setAttribute(
                                "modelAndView",
                                new ModelAndView(adminDashboardView, "adminUser", au))
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown response %s" format unknown)
                    }
                    case unknown => throw new RuntimeException("Unknown response %s" format unknown)
            }

            continuation.undispatch()

        })
    }
}
