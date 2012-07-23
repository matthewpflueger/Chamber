package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import com.echoed.chamber.services.partner.{RegisterPartner, RegisterPartnerResponse}
import javax.validation.Valid
import org.springframework.validation.BindingResult
import com.echoed.chamber.controllers.{FormController, EchoedController, Errors}
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.beans.factory.annotation.Autowired


@Controller
class RegisterController extends EchoedController with FormController {

    @Autowired var formValidator: RegisterFormValidator = _

    val defaultFieldPrefix = "registerForm."

    @RequestMapping(value = Array("/partner/register"), method = Array(RequestMethod.GET))
    def registerGet = new ModelAndView(v.registerView, "registerForm", new RegisterForm())


    @RequestMapping(value = Array("/partner/register"), method = Array(RequestMethod.POST))
    def registerPost(
            @Valid registerForm: RegisterForm,
            bindingResult: BindingResult) = {

        val errorModelAndView = new ModelAndView(v.registerView) with Errors

        if (!bindingResult.hasErrors) {
            formValidator.validate(registerForm, bindingResult)
        }

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            mp(registerForm.createPartner(RegisterPartner(_, _, _))).onComplete(_.fold(
                e => {
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                },
                _ match {
                    case RegisterPartnerResponse(_, Left(e)) =>
                        errorModelAndView.addError(e)
                        result.set(errorModelAndView)
                    case RegisterPartnerResponse(_, Right(partner)) =>
                        log.debug("Successfully registered partner {}", partner)
                        result.set(new ModelAndView("redirect:http://echoed.com"))
                }))

            result
        }
    }

}
