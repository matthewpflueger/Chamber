package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{RequestParam, RequestMapping}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.{Errors, CookieManager}


@Controller
class LoginController {

    private val logger = LoggerFactory.getLogger(classOf[LoginController])

    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var partnerLoginErrorView: String = _
    @BeanProperty var partnerLoginView: String = _


    @RequestMapping(value = Array("/partner/login"))
    def login(
            @RequestParam(value = "email", required = false) email: String,
            @RequestParam(value = "password", required = false) password: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val errorModelAndView = new ModelAndView(partnerLoginErrorView) with Errors
        if (email == null || password == null) {
            errorModelAndView
        } else {
            val result = new DeferredResult(errorModelAndView)

            logger.debug("Received login request for {}", email)

            partnerUserServiceLocator.login(email, password).onSuccess {
                case LoginResponse(_, Left(error)) =>
                    errorModelAndView.addError(error)
                    result.set(errorModelAndView)
                case LoginResponse(_, Right(pus)) => pus.getPartnerUser.onSuccess {
                    case GetPartnerUserResponse(_, Left(error)) =>
                        errorModelAndView.addError(error)
                        result.set(errorModelAndView)
                    case GetPartnerUserResponse(_, Right(pu)) =>
                        logger.debug("Successful login for {}", email)
                        cookieManager.addPartnerUserCookie(
                            httpServletResponse,
                            pu,
                            httpServletRequest)
                        result.set(new ModelAndView(partnerLoginView))
                }
            }

            result
        }
    }

}
