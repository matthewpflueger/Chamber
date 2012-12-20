package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.validation.Valid
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{RequestParam, PathVariable, RequestMapping, RequestMethod}
import com.echoed.chamber.services.partneruser._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.{FormController, EchoedController, Errors}
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.controllers.interceptors.Secure
import com.echoed.chamber.domain.InvalidPassword
import scala.concurrent.ExecutionContext.Implicits.global



@Controller
@Secure
class ActivateController extends EchoedController with FormController {

    @RequestMapping(value = Array("/partner/activate/{code}"), method = Array(RequestMethod.GET))
    def activateGet(
            @PathVariable("code") code: String,
            @RequestParam(value = "id", required = true) id: String,
            request: HttpServletRequest,
            response: HttpServletResponse) = {
        val mv = new ModelAndView(v.activateView)
        mv.addObject("activateForm", new ActivateForm())
        mv.addObject("code", code)
        mv.addObject("id", id)
    }

    @RequestMapping(value = Array("/partner/activate/{code}"), method = Array(RequestMethod.POST))
    def activatePost(
            @PathVariable("code") code: String,
            @RequestParam(value = "id", required = true) id: String,
            @Valid activateForm: ActivateForm,
            bindingResult: BindingResult) = {

        val errorModelAndView = new ModelAndView(v.activateView) with Errors
        errorModelAndView.addObject("code", code)
        errorModelAndView.addObject("id", id)

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {
            val result = new DeferredResult[ModelAndView]()

            log.debug("Activating partner user {}", id)

            mp(ActivatePartnerUser(new PartnerUserClientCredentials(id), code, activateForm.password)).onSuccess {
                case ActivatePartnerUserResponse(_, Left(e: InvalidPassword)) =>
                    bindingResult.rejectValue("password", e.code.get, e.message)
                    result.setResult(errorModelAndView)
                case ActivatePartnerUserResponse(_, Right(pucc)) =>
                    val modelAndView = new ModelAndView(v.postActivateView)
                    modelAndView.addObject("partnerUser", pucc)
                    result.setResult(modelAndView)
                    log.debug("Activated partner user {}: {}", pucc.id, pucc.name)
                case ActivatePartnerUserResponse(_, Left(e)) =>
                    errorModelAndView.addError(e)
                    result.setResult(errorModelAndView)
            }

            result
        }
    }

}
