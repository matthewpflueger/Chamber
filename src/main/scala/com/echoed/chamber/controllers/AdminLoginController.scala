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
@RequestMapping(Array("/admin"))
class AdminLoginController {

    @BeanProperty var adminUserServiceLocator: AdminUserServiceLocator = _

    @BeanProperty var adminLoginView: String = _
    @BeanProperty var adminDashboardView: String = _
    @BeanProperty var adminDashboardErrorView: String = _

    @BeanProperty var cookieManager: CookieManager = _

    private final val logger = LoggerFactory.getLogger(classOf[AdminLoginController])
    
    @RequestMapping(Array("/create"))
    @ResponseBody
    def create(
            @RequestParam(value="email", required = true) email:String,
            @RequestParam(value="password", required = true) password:String,
            @RequestParam(value="name", required = true) name: String,
            @RequestParam(value="token", required = true) token: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        logger.debug("Creating Admin Account for {}, {}", name, email)
        assert(token == "J0n1sR3tard3d")

        adminUserServiceLocator.create(email,name,password).onSuccess {
            case CreateAdminUserResponse(_, Right(adminUser)) => result.set(adminUser)
        }

        result
    }
    
    @RequestMapping(Array("/login"))
    def login(
            @RequestParam(value="email", required = false) email:String,
            @RequestParam(value="password", required = false) password: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(adminLoginView))

        adminUserServiceLocator.login(email,password).onSuccess {
            case LoginResponse(_, Right(adminUserService)) =>
                logger.debug("Redirecting to Admin Dashboard View {}", adminDashboardView)
                adminUserService.getAdminUser.onSuccess {
                    case GetAdminUserResponse(_, Right(adminUser)) =>
                        cookieManager.addAdminUserCookie(
                                httpServletResponse,
                                adminUser,
                                httpServletRequest)
                        result.set(new ModelAndView(adminDashboardView))
                }
        }

        result
    }
}