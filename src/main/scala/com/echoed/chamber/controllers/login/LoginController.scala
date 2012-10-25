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
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials => EUCC}
import com.echoed.chamber.services.echoeduser._
import scala.Left
import com.echoed.chamber.services.echoeduser.RegisterLogin
import com.echoed.chamber.services.echoeduser.RegisterLoginResponse
import scala.Right
import com.echoed.chamber.controllers.interceptors.Secure
import javax.annotation.Nullable



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

            val view = "%s?redirect=%s" format(v.postLoginView, Option(redirect).getOrElse(""))

            mp(LoginWithPassword(EchoedUserClientCredentials(lf.cred, password = Some(lf.password)))).onSuccess {
                case LoginWithPasswordResponse(_, Left(e)) =>
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                case LoginWithPasswordResponse(_, Right(echoedUser)) =>
                    cookieManager.addEchoedUserCookie(response, echoedUser, request)
                    result.set(new ModelAndView(view))
            }

            result
        }
    }


    @RequestMapping(value = Array("/register"), method = Array(RequestMethod.GET))
    def registerGet(
            @RequestParam(value = "redirect", required = false) redirect: String,
            @Nullable eucc: EUCC,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val view = Option(redirect).map { redirect =>
            "redirect:%s/%s" format (v.secureSiteUrl, redirect)
        }.getOrElse("redirect:%s" format v.siteUrl)

        if (Option(eucc).exists(_.isComplete)) new ModelAndView(view)
        else {
            //this is a hack to get around having every controller know about registration for now...
            //basically all the login controllers add cookie but we remove it here if the user needs to register...
            cookieManager.addEchoedUserCookie(response, request = request)

            val modelAndView = new ModelAndView(v.loginRegisterView, "loginRegisterForm", new LoginRegisterForm(Option(eucc)))
            modelAndView.addObject("redirect", redirect)
        }
    }

    @RequestMapping(value = Array("/register"), method = Array(RequestMethod.POST))
    def registerPost(
            @Valid lrf: LoginRegisterForm,
            bindingResult: BindingResult,
            @RequestParam(value = "redirect", required = false) redirect: String,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val errorModelAndView = new ModelAndView(v.loginRegisterView) with Errors
        errorModelAndView.addObject("redirect", redirect)

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)
            val view = Option(redirect).map { redirect =>
                "redirect:%s/%s" format (v.secureSiteUrl, redirect)
            }.getOrElse("redirect:%s" format v.siteUrl)

            val eucc = Option(lrf.echoedUserId).map(EUCC(_))

            mp(RegisterLogin(lrf.name, lrf.email, lrf.screenName, lrf.password, eucc)).onSuccess {
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
            mp(ResetLogin(EUCC(loginResetForm.cred)))
            new ModelAndView(v.loginResetPostView)
        }
    }


    @RequestMapping(value = Array("/reset/{code}"), method = Array(RequestMethod.GET))
    def resetPasswordGet(
            @PathVariable("code") code: String,
            @RequestParam(value = "id", required = true) id: String,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val mv = new ModelAndView(v.loginResetPasswordView)
        mv.addObject("resetPasswordForm", new ResetPasswordForm())
        mv.addObject("code", code)
        mv.addObject("id", id)
    }


    @RequestMapping(value = Array("/reset/password/{code}"), method = Array(RequestMethod.POST))
    def resetPasswordPost(
            @Valid resetPasswordForm: ResetPasswordForm,
            bindingResult: BindingResult,
            @PathVariable("code") code: String,
            @RequestParam(value = "id", required = true) id: String,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val errorModelAndView = new ModelAndView(v.loginResetPasswordView) with Errors
        errorModelAndView.addObject("code", code)
        errorModelAndView.addObject("id", id)

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            mp(ResetPassword(EUCC(id), code, resetPasswordForm.password)).onSuccess {
                case ResetPasswordResponse(_, Left(e)) =>
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                case ResetPasswordResponse(_, Right(echoedUser)) =>
                    cookieManager.addEchoedUserCookie(response, echoedUser, request)
                    result.set(new ModelAndView("redirect:%s" format v.siteUrl))
            }

            result
        }
    }

    @RequestMapping(value = Array("/verify/{code}"), method = Array(RequestMethod.GET))
    def verifyGet(
            @PathVariable("code") code: String,
            @RequestParam(value = "id", required = true) id: String,
            response: HttpServletResponse,
            request: HttpServletRequest) = {

        val mv = new ModelAndView("redirect:%s" format v.siteUrl)
        val result = new DeferredResult(mv)

        mp(VerifyEmail(EUCC(id), code)).onSuccess {
            case VerifyEmailResponse(_, Right(echoedUser)) =>
                cookieManager.addEchoedUserCookie(response, echoedUser, request)
                result.set(mv)
        }

        result
    }
}
