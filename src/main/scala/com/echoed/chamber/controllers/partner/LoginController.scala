package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{RequestParam, RequestMapping}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.{EchoedController, Errors}
import com.echoed.chamber.controllers.interceptors.Secure


@Controller("partnerLogin")
class LoginController extends EchoedController {

    @Secure
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
            val result = new DeferredResult[ModelAndView](null, errorModelAndView)

            log.debug("Received login request for {}", email)

            mp(LoginWithEmailPassword(email, password)).onSuccess {
                case LoginWithEmailPasswordResponse(_, Left(error)) =>
                    errorModelAndView.addError(error)
                    result.setResult(errorModelAndView)
                case LoginWithEmailPasswordResponse(_, Right(pu)) =>
                    log.debug("Successful login for {}", email)
                    cookieManager.addPartnerUserCookie(
                        response,
                        pu,
                        request)
                    result.setResult(new ModelAndView(v.partnerLoginView))
            }

            result
        }
    }

}
