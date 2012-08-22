package com.echoed.chamber.controllers.login

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.validation.Valid
import org.springframework.validation.BindingResult
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.{Errors, FormController, EchoedController}
import scala._
import com.echoed.chamber.services.echoeduser._
import scala.Left
import com.echoed.chamber.services.echoeduser.RegisterLogin
import com.echoed.chamber.services.echoeduser.RegisterLoginResponse
import com.echoed.chamber.services.echoeduser.LoginWithEmail
import scala.Right
import com.echoed.chamber.services.echoeduser.LoginWithEmailPasswordResponse


@Controller("echoedUserLogin")
@RequestMapping(Array("/login"))
class LoginController extends EchoedController with FormController {

    @RequestMapping(method = Array(RequestMethod.GET))
    def loginGet = new ModelAndView(v.loginEmailView, "loginForm", new LoginForm())

    @RequestMapping(method = Array(RequestMethod.POST))
    def loginPost(
            @Valid lf: LoginForm,
            bindingResult: BindingResult,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val errorModelAndView = new ModelAndView(v.loginEmailView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            mp(LoginWithEmailPassword(lf.email, lf.password)).onSuccess {
                case LoginWithEmailPasswordResponse(_, Left(e)) =>
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                case LoginWithEmailPasswordResponse(_, Right(echoedUser)) =>
                    cookieManager.addEchoedUserCookie(response, echoedUser, request)
                    result.set(new ModelAndView("redirect:%s" format v.siteUrl))
            }

            result
        }
    }


    @RequestMapping(value = Array("/register"), method = Array(RequestMethod.GET))
    def registerGet = new ModelAndView(v.loginRegisterView, "loginRegisterForm", new LoginRegisterForm())

    @RequestMapping(value = Array("/register"), method = Array(RequestMethod.POST))
    def registerPost(
            @Valid lrf: LoginRegisterForm,
            bindingResult: BindingResult,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val errorModelAndView = new ModelAndView(v.loginRegisterView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            mp(RegisterLogin(lrf.name, lrf.email, lrf.password)).onSuccess {
                case RegisterLoginResponse(_, Left(e)) =>
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                case RegisterLoginResponse(_, Right(echoedUser)) =>
                    cookieManager.addEchoedUserCookie(response, echoedUser, request)
                    result.set(new ModelAndView("redirect:%s" format v.siteUrl))
            }

            result
        }
    }


    @RequestMapping(value = Array("/reset"), method = Array(RequestMethod.GET))
    def resetGet = new ModelAndView(v.loginResetView, "loginResetForm", new LoginResetForm())

    @RequestMapping(value = Array("/reset"), method = Array(RequestMethod.POST))
    def resetPost(
            @Valid loginResetForm: LoginResetForm,
            bindingResult: BindingResult) = {

        val errorModelAndView = new ModelAndView(v.loginResetView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            mp(ResetLogin(loginResetForm.email)).onSuccess {
                case ResetLoginResponse(_, Left(e)) =>
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                case ResetLoginResponse(_, Right(_)) =>
                    result.set(new ModelAndView(v.loginResetPostView, "email", loginResetForm.email))
            }

            result
        }
    }


    @RequestMapping(value = Array("/reset/{code}"), method = Array(RequestMethod.GET))
    def resetPasswordGet(
            @PathVariable("code") code: String,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val result = new DeferredResult(new ModelAndView("redirect:%s" format v.siteUrl))

        mp(LoginWithCode(code)).onSuccess {
            case LoginWithCodeResponse(_, Right(echoedUser)) =>
                val modelAndView = new ModelAndView(v.loginResetPasswordView)
                modelAndView.addObject("resetPasswordForm", new ResetPasswordForm())
                modelAndView.addObject("echoedUser", echoedUser)
                cookieManager.addEchoedUserCookie(response, echoedUser, request)
                result.set(modelAndView)
        }

        result
    }


    @RequestMapping(value = Array("/reset/password"), method = Array(RequestMethod.POST))
    def resetPasswordPost(
            @Valid resetPasswordForm: ResetPasswordForm,
            bindingResult: BindingResult,
            eucc: EchoedUserClientCredentials) = {

        val errorModelAndView = new ModelAndView(v.loginResetPasswordView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            mp(ResetPassword(eucc, resetPasswordForm.password)).onSuccess {
                case ResetPasswordResponse(_, Left(e)) =>
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                case ResetPasswordResponse(_, Right(_)) =>
                    result.set(new ModelAndView("redirect:%s" format v.siteUrl))
            }

            result
        }
    }

}
