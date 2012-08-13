package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{RequestParam, RequestMapping}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.{EchoedController, Errors}


@Controller
class LoginController extends EchoedController {

    @RequestMapping(value = Array("/partner/login"))
    def login(
            @RequestParam(value = "email", required = false) email: String,
            @RequestParam(value = "password", required = false) password: String,
            request: HttpServletRequest,
            response: HttpServletResponse) = {

        val errorModelAndView = new ModelAndView(v.partnerLoginErrorView) with Errors
        if (email == null || password == null) {
            errorModelAndView
        } else {
            val result = new DeferredResult(errorModelAndView)

            log.debug("Received login request for {}", email)

            mp(Login(email, password)).onSuccess {
                case LoginResponse(_, Left(error)) =>
                    errorModelAndView.addError(error)
                    result.set(errorModelAndView)
                case LoginResponse(_, Right(pu)) =>
                    log.debug("Successful login for {}", email)
                    cookieManager.addPartnerUserCookie(
                        response,
                        pu,
                        request)
                    result.set(new ModelAndView(v.partnerLoginView))
            }

            result
        }
    }

}
