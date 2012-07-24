package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.{InitBinder, RequestMapping, RequestMethod}
import com.echoed.chamber.services.partner.{RegisterPartnerResponse, PartnerServiceManager}
import javax.validation.Valid
import org.springframework.validation.{Validator, BindingResult}
import org.springframework.core.convert.ConversionService
import com.echoed.chamber.controllers.{Errors, CookieManager}
import org.springframework.web.context.request.async.DeferredResult


@Controller
class RegisterController {

    private val logger = LoggerFactory.getLogger(classOf[RegisterController])

    @BeanProperty var partnerServiceManager: PartnerServiceManager = _

    @BeanProperty var registerView: String = _
    @BeanProperty var postRegisterView: String = _
    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var globalValidator: Validator = _
    @BeanProperty var formValidator: Validator = _
    @BeanProperty var conversionService: ConversionService = _

    @InitBinder
    def initBinder(binder: WebDataBinder) {
        binder.setFieldDefaultPrefix("registerForm.")
        binder.setValidator(globalValidator)
        binder.setConversionService(conversionService)
    }

    @RequestMapping(value = Array("/partner/register"), method = Array(RequestMethod.GET))
    def registerGet(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        new ModelAndView(registerView, "registerForm", new RegisterForm())
    }

    @RequestMapping(value = Array("/partner/register"), method = Array(RequestMethod.POST))
    def registerPost(
            @Valid registerForm: RegisterForm,
            bindingResult: BindingResult,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val errorModelAndView = new ModelAndView(registerView) with Errors

        if (!bindingResult.hasErrors) {
            formValidator.validate(registerForm, bindingResult)
        }

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            registerForm.createPartner(partnerServiceManager.registerPartner(_, _, _)).onComplete(_.fold(
                e => {
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                },
                _ match {
                    case RegisterPartnerResponse(_, Left(e)) =>
                        errorModelAndView.addError(e)
                        result.set(errorModelAndView)
                    case RegisterPartnerResponse(_, Right(partner)) =>
                        logger.debug("Successfully registered partner {}", partner)
                        result.set(new ModelAndView("redirect:http://echoed.com"))
                }))

            result
        }
    }

}
