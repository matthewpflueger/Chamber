package com.echoed.chamber.controllers.partner.magentogo

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.validation.Valid
import org.springframework.validation.BindingResult
import com.echoed.chamber.services.partner.magentogo.{RegisterMagentoGoPartner, RegisterMagentoGoPartnerResponse}
import com.echoed.chamber.controllers.{FormController, EchoedController, Errors}
import org.springframework.web.context.request.async.DeferredResult


@Controller
@RequestMapping(Array("/magentogo"))
class MagentoGoController extends EchoedController with FormController {

    def defaultFieldPrefix = "registerForm."

    @RequestMapping(method = Array(RequestMethod.GET))
    def registerGet = new ModelAndView(v.magentoGoRegisterView, "registerForm", new RegisterForm())


    @RequestMapping(method = Array(RequestMethod.POST))
    def registerPost(
            @Valid registerForm: RegisterForm,
            bindingResult: BindingResult) = {

        val errorModelAndView = new ModelAndView(v.magentoGoRegisterView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {
            val result = new DeferredResult(errorModelAndView)

            mp(RegisterMagentoGoPartner(registerForm.createPartner)).onComplete(_.fold(
                e => {
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                },
                _ match {
                    case RegisterMagentoGoPartnerResponse(_, Left(e)) =>
                        errorModelAndView.addError(e)
                        result.set(errorModelAndView)
                    case RegisterMagentoGoPartnerResponse(_, Right(r)) =>
                        log.debug("Successfully registered MagentoGo partner {}", r.partner.name)
                        val modelAndView = new ModelAndView(v.magentoGoPostRegisterView)
                        modelAndView.addObject("partner", r.partner)
                        modelAndView.addObject("partnerUser", r.partnerUser)
                        modelAndView.addObject("magentoGoPartner", r.magentoGoPartner)
                        result.set(modelAndView)
                }))

            result
        }
    }

}
