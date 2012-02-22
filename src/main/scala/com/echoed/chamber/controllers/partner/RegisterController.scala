package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.{InitBinder, RequestMapping, RequestMethod}
import com.echoed.chamber.services.partner.{RegisterPartnerResponse, PartnerServiceManager}
import com.echoed.chamber.controllers.{Errors, RequestExpiredException, CookieManager}
import javax.validation.Valid
import org.springframework.validation.{Validator, BindingResult}
import org.springframework.core.convert.ConversionService
import com.echoed.chamber.controllers.ControllerUtils.error


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

        if (!bindingResult.hasErrors) {
            formValidator.validate(registerForm, bindingResult)
        }

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(registerView, Some(RequestExpiredException()))
        } else if (bindingResult.hasErrors) {
            error(registerView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            registerForm.createPartner(partnerServiceManager.registerPartner(_, _, _)).onComplete(_.value.get.fold(
                e => error(registerView, Some(e)),
                _ match {
                    case RegisterPartnerResponse(_, Left(e)) => error(registerView, Some(e))
                    case RegisterPartnerResponse(_, Right(partner)) =>
                        logger.debug("Successfully registered partner {}", partner)
                        //FIXME!!!
                        continuation.setAttribute("modelAndView", new ModelAndView("redirect:http://www.echoed.com"))
                        continuation.resume
                }
            ))

            continuation.undispatch()
        })

    }

}
