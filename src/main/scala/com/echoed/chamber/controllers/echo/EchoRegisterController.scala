package com.echoed.chamber.controllers.echo

import scala.Array
import com.echoed.chamber.controllers.{FormController, EchoedController}
import org.springframework.stereotype.Controller
import javax.validation.Valid
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{RequestParam, RequestMethod, RequestMapping}
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.echoeduser.{UpdateEchoedUserEmail, EchoedUserClientCredentials, EmailAlreadyExists}

import org.springframework.web.context.request.async.DeferredResult
import org.springframework.beans.factory.annotation.Autowired


@Controller
class EchoRegisterController extends EchoedController with FormController {

    @Autowired var formValidator: EchoRegisterFormValidator = _

    override val defaultFieldPrefix = ("registerForm.")

    @RequestMapping(value = Array("/echo/register"), method = Array(RequestMethod.GET))
    def details(
            @RequestParam(value = "id", required = true) id: String,
            @RequestParam(value= "close", required = false) close: Boolean) = {

        val modelAndView = new ModelAndView(v.echoRegisterView)
        modelAndView.addObject("id",id)
        if (close == true) modelAndView.addObject("close", "true")

        modelAndView
    }

    @RequestMapping(value = Array("echo/register"), method = Array(RequestMethod.POST))
    def detailsPost(
            @RequestParam(value = "id", required = true) id: String,
            @RequestParam(value= "close", required = false) close: Boolean,
            @Valid echoRegisterForm: EchoRegisterForm,
            bindingResult: BindingResult,
            eucc: EchoedUserClientCredentials) = {

        val errorModelAndView = new ModelAndView(v.echoRegisterView)
        if (close == true) errorModelAndView.addObject("close", "true")
        errorModelAndView.addObject("id", id)

        if (!bindingResult.hasErrors) {
            formValidator.validate(echoRegisterForm, bindingResult)
        }

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {
            val result = new DeferredResult[ModelAndView](null, errorModelAndView)

            mp(UpdateEchoedUserEmail(eucc, echoRegisterForm.getEmail)).foreach(_.cata(
                _ match {
                    case EmailAlreadyExists(em, _ , _) =>
                        log.debug("Email already exists: {}", em)
                        val modelAndView = new ModelAndView(v.echoRegisterView)
                        modelAndView.addObject("id", id)
                        if (close == true) modelAndView.addObject("close", "true")
                        modelAndView.addObject("error_email", "This Email Already Exists")
                        result.setResult(modelAndView)
                },
                _ => {
                    if (close) result.setResult(new ModelAndView(v.echoCloseViewUrl, "id", id))
                    else result.setResult(new ModelAndView(v.echoEchoedViewUrl, "id", id))
                }))

            result
        }
    }

}