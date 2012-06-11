package com.echoed.chamber.controllers.partner.bigcommerce

import org.springframework.stereotype.Controller
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.WebDataBinder
import javax.validation.Valid
import org.springframework.validation.{Validator, BindingResult}
import org.springframework.core.convert.ConversionService
import com.echoed.chamber.controllers.ControllerUtils.error
import com.echoed.chamber.services.partner.bigcommerce.{RegisterBigCommercePartnerResponse, BigCommercePartnerServiceManager}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.{Errors, RequestExpiredException}


@Controller
@RequestMapping(Array("/bigcommerce"))
class BigCommerceController {

    @BeanProperty var bigCommercePartnerServiceManager: BigCommercePartnerServiceManager = _

    @BeanProperty var registerView: String = _
    @BeanProperty var postRegisterView: String = _
    @BeanProperty var globalValidator: Validator = _
    @BeanProperty var conversionService: ConversionService = _

    @BeanProperty var successUrl: String = _

    private val logger = LoggerFactory.getLogger(classOf[BigCommerceController])


    @InitBinder
    def initBinder(binder: WebDataBinder) {
        binder.setFieldDefaultPrefix("registerForm.")
        binder.setValidator(globalValidator)
        binder.setConversionService(conversionService)
    }


    @RequestMapping(method = Array(RequestMethod.GET))
    def registerGet(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        new ModelAndView(registerView, "registerForm", new RegisterForm())
    }

    @RequestMapping(method = Array(RequestMethod.POST))
    def registerPost(
            @Valid registerForm: RegisterForm,
            bindingResult: BindingResult,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val errorModelAndView = new ModelAndView(registerView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {
            val result = new DeferredResult(errorModelAndView)

            bigCommercePartnerServiceManager.registerPartner(registerForm.createPartner).onComplete(_.fold(
                e => {
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                },
                _ match {
                    case RegisterBigCommercePartnerResponse(_, Left(e)) =>
                        errorModelAndView.addError(e)
                        result.set(errorModelAndView)
                    case RegisterBigCommercePartnerResponse(_, Right(r)) =>
                        logger.debug("Successfully registered BigCommerce partner {}", r.partner.name)
                        val modelAndView = new ModelAndView(postRegisterView)
                        modelAndView.addObject("partner", r.partner)
                        modelAndView.addObject("partnerUser", r.partnerUser)
                        modelAndView.addObject("bigCommercePartner", r.bigCommercePartner)
                        result.set(modelAndView)
                }))

            result
        }
    }

}
