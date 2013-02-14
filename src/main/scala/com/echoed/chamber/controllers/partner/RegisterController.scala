package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import com.echoed.chamber.services.partner._
import javax.validation.Valid
import org.springframework.validation.BindingResult
import com.echoed.chamber.controllers.{FormController, EchoedController, Errors}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.interceptors.Secure
import com.echoed.util.ScalaObjectMapper
import scala.Left
import com.echoed.chamber.services.partner.RegisterPartner
import scala.Right
import scala.Some
import scala.concurrent.ExecutionContext.Implicits.global


@Controller
@Secure
class RegisterController extends EchoedController with FormController {

    @RequestMapping(value = Array("/partner/register"), method = Array(RequestMethod.GET))
    def registerGet = {
        val form = new RegisterForm
        form.communitiesList = new ScalaObjectMapper().writeValueAsString(form.communities.map(_.name))
        new ModelAndView(v.registerView, "registerForm", form)
    }


    @RequestMapping(value = Array("/partner/register"), method = Array(RequestMethod.POST))
    def registerPost(
            @Valid form: RegisterForm,
            bindingResult: BindingResult) = {

        val errorModelAndView = new ModelAndView(v.registerView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult[ModelAndView](null, errorModelAndView)

            mp(RegisterPartner(
                    form.userName,
                    form.email,
                    form.siteName,
                    form.siteUrl,
                    form.shortName,
                    form.community)).onSuccess {
                case RegisterPartnerResponse(_, Left(e)) =>
                    errorModelAndView.addError(e, Some("registerForm"))
                    result.setResult(errorModelAndView)
                case RegisterPartnerResponse(_, Right((partnerUser, partner))) =>
                    val mav = new ModelAndView(v.postRegisterView)
                    mav.addObject("partnerUser", partnerUser)
                    mav.addObject("partner", partner)
                    result.setResult(mav)
            }

            result
        }
    }

}
