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
import scala.Right
import com.echoed.chamber.services.echoeduser.LoginWithEmailPasswordResponse
import com.echoed.chamber.controllers.interceptors.Secure
import javax.annotation.Nullable
import scalaz._
import Scalaz._


@Controller("echoedUserLogin")
@RequestMapping(Array("/login"))
@Secure
class LoginController extends EchoedController with FormController {

    @RequestMapping(method = Array(RequestMethod.GET))
    def loginGet(
            @RequestParam(value = "redirect", required = false) redirect: String,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val modelAndView = new ModelAndView(v.loginEmailView, "loginForm", new LoginForm())
        modelAndView.addObject("redirect", redirect)
        modelAndView
    }

    @RequestMapping(method = Array(RequestMethod.POST))
    def loginPost(
            @Valid lf: LoginForm,
            bindingResult: BindingResult,
            @RequestParam (value = "redirect", required = false) redirect: String,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val errorModelAndView = new ModelAndView(v.loginEmailView) with Errors
        errorModelAndView.addObject("redirect", redirect)

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            val view = "%s/%s" format(v.postLoginView, Option(redirect).getOrElse(""))
//            val view = Option(r).map { redirect =>
//                "redirect:%s/%s" format (v.secureSiteUrl, redirect)
//            }.getOrElse("redirect:%s" format v.siteUrl)

            mp(LoginWithEmailPassword(lf.email, lf.password)).onSuccess {
                case LoginWithEmailPasswordResponse(_, Left(e)) =>
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                case LoginWithEmailPasswordResponse(_, Right(echoedUser)) =>
                    cookieManager.addEchoedUserCookie(response, echoedUser, request)
                    result.set(new ModelAndView(view))
            }

            result
        }
    }


    @RequestMapping(value = Array("/register"), method = Array(RequestMethod.GET))
    def registerGet(
            @RequestParam(value = "redirect", required = false) r: String,
            @Nullable eucc: EchoedUserClientCredentials,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val view = Option(r).map { redirect =>
            "redirect:%s/%s" format (v.secureSiteUrl, redirect)
        }.getOrElse("redirect:%s" format v.siteUrl)

        if (Option(eucc).exists(_.isComplete)) new ModelAndView(view)
        else {
            val modelAndView = new ModelAndView(v.loginRegisterView, "loginRegisterForm", new LoginRegisterForm(Option(eucc)))
            modelAndView.addObject("redirect", r)
            modelAndView.addObject("eucc", eucc)
            modelAndView.addObject("password", Option(eucc).map(_.password))
        }
    }

    @RequestMapping(value = Array("/register"), method = Array(RequestMethod.POST))
    def registerPost(
            @Valid lrf: LoginRegisterForm,
            bindingResult: BindingResult,
            @RequestParam(value = "redirect", required = false) redirect: String,
            @Nullable eucc: EchoedUserClientCredentials,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val errorModelAndView = new ModelAndView(v.loginRegisterView) with Errors
        errorModelAndView.addObject("eucc", eucc)
        errorModelAndView.addObject("password", Option(eucc).map(_.password))
        errorModelAndView.addObject("redirect", redirect)

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)
            val view = Option(redirect).map { redirect =>
                "redirect:%s/%s" format (v.secureSiteUrl, redirect)
            }.getOrElse("redirect:%s" format v.siteUrl)

            mp(RegisterLogin(lrf.name, lrf.email, lrf.screenName, lrf.password, Option(eucc))).onSuccess {
                case RegisterLoginResponse(_, Left(e)) =>
                    bindingResult.addAllErrors(e.asErrors(Some("loginRegisterForm")))
//                    errorModelAndView.addError(e, Some("loginRegisterForm"))
                    result.set(errorModelAndView)
                case RegisterLoginResponse(_, Right(echoedUser)) =>
                    cookieManager.addEchoedUserCookie(response, echoedUser, request)
                    result.set(new ModelAndView(view))
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

    @RequestMapping(value = Array("/verify/{code}"), method = Array(RequestMethod.GET))
    def verifyGet(
            @PathVariable("code") code: String,
            response: HttpServletResponse,
            request: HttpServletRequest) = {
        mp(VerifyEmail(code))
        new ModelAndView("redirect:%s" format v.siteUrl)
    }
}
