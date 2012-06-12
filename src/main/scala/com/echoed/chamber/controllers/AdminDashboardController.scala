package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.ContinuationSupport
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.adminuser._
import org.springframework.web.context.request.async.DeferredResult


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


        val result = new DeferredResult(new ModelAndView(adminDashboardErrorView))

        adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
                case LocateAdminUserServiceResponse(_, Right(adminUserService)) => adminUserService.getAdminUser.onSuccess {
                    case GetAdminUserResponse(_, Right(au)) =>
                        logger.debug("Got {}", au)
                        result.set(new ModelAndView(adminDashboardView, "adminUser", au))
                }
        }
        result
    }
}
