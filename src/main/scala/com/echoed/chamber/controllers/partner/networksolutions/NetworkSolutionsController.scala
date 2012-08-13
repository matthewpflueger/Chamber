package com.echoed.chamber.controllers.partner.networksolutions

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.validation.Valid
import org.springframework.validation.BindingResult
import com.echoed.chamber.services.partner.networksolutions._
import com.echoed.chamber.controllers.{FormController, EchoedController, Errors}
import org.springframework.web.context.request.async.DeferredResult
import scala.Left
import com.echoed.chamber.services.partner.networksolutions.RegisterNetworkSolutionsPartner
import com.echoed.chamber.services.partner.networksolutions.RegisterNetworkSolutionsPartnerResponse
import com.echoed.chamber.services.partner.networksolutions.AuthNetworkSolutionsPartnerResponse
import scala.Right


@Controller
@RequestMapping(Array("/networksolutions"))
class NetworkSolutionsController extends EchoedController with FormController {

    def defaultFieldPrefix = "registerForm."

    @RequestMapping(method = Array(RequestMethod.GET))
    def registerGet = new ModelAndView(v.networkSolutionsRegisterView, "registerForm", new RegisterForm())


    @RequestMapping(method = Array(RequestMethod.POST))
    def registerPost(
            @Valid registerForm: RegisterForm,
            bindingResult: BindingResult) = {

        val errorModelAndView = new ModelAndView(v.networkSolutionsRegisterView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {
            val result = new DeferredResult(errorModelAndView)

            mp(RegisterNetworkSolutionsPartner(registerForm.name, registerForm.email, registerForm.phone, v.networkSolutionsSuccessUrl)).onComplete(_.fold(
                e => {
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                },
                _ match {
                    case RegisterNetworkSolutionsPartnerResponse(_, Left(e)) =>
                        errorModelAndView.addError(e)
                        result.set(errorModelAndView)
                    case RegisterNetworkSolutionsPartnerResponse(_, Right(loginUrl)) =>
                        log.debug("Successfully registered Network Solutions partner {}, login url is {}", registerForm.email, loginUrl)
                        result.set(new ModelAndView("redirect:%s" format loginUrl))
                }))

            result
        }
    }


    @RequestMapping(value = Array("/auth"), method = Array(RequestMethod.GET))
    def auth(@RequestParam(value = "userkey", required = true) userKey: String) = {
        val result = new DeferredResult(new ModelAndView("error"))

        log.debug("Attempting to authorize Network Solutions partner {}", userKey)
        mp(AuthNetworkSolutionsPartner(userKey)).onSuccess {
            case AuthNetworkSolutionsPartnerResponse(_, Right(envelope)) =>
                log.debug("Successfully authorized {}", envelope.networkSolutionsPartner)
                val modelAndView = new ModelAndView(v.networkSolutionsPostAuthView)
                modelAndView.addObject("networkSolutionsPartner", envelope.networkSolutionsPartner)
                modelAndView.addObject("partnerUser", envelope.partnerUser)
                modelAndView.addObject("partner", envelope.partner)
                result.set(modelAndView)
        }

        result
    }

}
