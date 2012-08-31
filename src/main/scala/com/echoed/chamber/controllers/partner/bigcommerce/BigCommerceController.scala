package com.echoed.chamber.controllers.partner.bigcommerce

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.validation.Valid
import org.springframework.validation.BindingResult
import com.echoed.chamber.services.partner.bigcommerce.{RegisterBigCommercePartner, RegisterBigCommercePartnerResponse}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.{EchoedController, Errors}


@Controller
@RequestMapping(Array("/bigcommerce"))
class BigCommerceController extends EchoedController {

    def defaultFieldPrefix = "registerForm."

    @RequestMapping(method = Array(RequestMethod.GET))
    def registerGet = new ModelAndView(v.bigCommerceRegisterView, "registerForm", new RegisterForm())


    @RequestMapping(method = Array(RequestMethod.POST))
    def registerPost(
            @Valid registerForm: RegisterForm,
            bindingResult: BindingResult) = {

        val errorModelAndView = new ModelAndView(v.bigCommerceRegisterView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {
            val result = new DeferredResult(errorModelAndView)

            mp(RegisterBigCommercePartner(registerForm.createPartner)).onComplete(_.fold(
                e => {
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                },
                _ match {
                    case RegisterBigCommercePartnerResponse(_, Left(e)) =>
                        errorModelAndView.addError(e)
                        result.set(errorModelAndView)
                    case RegisterBigCommercePartnerResponse(_, Right(r)) =>
                        log.debug("Successfully registered BigCommerce partner {}", r.partner.name)
                        val modelAndView = new ModelAndView(v.bigCommercePostRegisterView)
                        modelAndView.addObject("partner", r.partner)
                        modelAndView.addObject("partnerUser", r.partnerUser)
                        modelAndView.addObject("bigCommercePartner", r.bigCommercePartner)
                        result.set(modelAndView)
                }))

            result
        }
    }

}
