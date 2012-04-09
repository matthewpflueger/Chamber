package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.ContinuationSupport
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.bind.annotation._
import com.echoed.chamber.domain.EchoClick
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.adminuser._


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/6/12
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */

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
        @RequestParam(value="email") email:String,
        @RequestParam(value="password") password:String, 
        @RequestParam(value="name") name: String,
        @RequestParam(value="token") token: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(continuation.isExpired){
            logger.debug("Continuation Debug")
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)
            logger.debug("Creating Admin Account:")
            if(email != null && password != null && name != null) {
                def onError(error: AdminUserException){
                    logger.debug("Error during admin creation")
                    continuation.setAttribute("modelAndView", error)
                    continuation.resume()
                }
                adminUserServiceLocator.create(email,name,password).onResult({
                    case CreateAdminUserResponse(_, Left(error)) => onError(error)
                    case CreateAdminUserResponse(_, Right(adminUser)) =>
                        continuation.setAttribute("modelAndView", adminUser)
                        continuation.resume()
                })
            }
            continuation.undispatch()
        })
        
    }
    
    @RequestMapping(Array("/login"))
    def login(
        @RequestParam(value="email", required=false) email:String,
        @RequestParam(value="password", required=false) password: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(continuation.isExpired){
            
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            if(email != null && password != null){

                def onError(error: AdminUserException) {
                    logger.debug("Got error during login for {}: {}", email, password)
                    continuation.setAttribute("modelAndView", adminLoginView)
                    continuation.resume()
                }

                adminUserServiceLocator.login(email,password).onResult({
                    case LoginResponse(_, Left(error)) => onError(error)
                    case LoginResponse(_, Right(adminUserService)) =>
                        logger.debug("Redirecting to Admin Dashboard View {}", adminDashboardView)
                        adminUserService.getAdminUser.onResult({
                            case GetAdminUserResponse(_, Left(e)) =>
                            case GetAdminUserResponse(_, Right(adminUser)) =>
                                cookieManager.addAdminUserCookie(
                                        httpServletResponse,
                                        adminUser,
                                        httpServletRequest)
                                continuation.setAttribute("modelAndView", new ModelAndView(adminDashboardView))
                                continuation.resume()
                        })
                })

            } else{
                continuation.setAttribute("modelAndView",adminLoginView)
                continuation.resume()
            }
            continuation.undispatch()

        })
    }
}