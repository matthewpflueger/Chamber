package com.echoed.chamber.controllers.partner.magentogo

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
import com.echoed.chamber.controllers.RequestExpiredException
import com.echoed.chamber.controllers.ControllerUtils.error
import com.echoed.chamber.services.partner.bigcommerce.{RegisterBigCommercePartnerResponse, BigCommercePartnerServiceManager}
import com.echoed.chamber.services.partner.magentogo.{RegisterMagentoGoPartnerResponse, MagentoGoPartnerServiceManager}


@Controller
@RequestMapping(Array("/magentogo"))
class MagentoGoController {

    @BeanProperty var magentoGoPartnerServiceManager: MagentoGoPartnerServiceManager = _

    @BeanProperty var registerView: String = _
    @BeanProperty var postRegisterView: String = _
    @BeanProperty var globalValidator: Validator = _
    @BeanProperty var conversionService: ConversionService = _

    @BeanProperty var successUrl: String = _

    private val logger = LoggerFactory.getLogger(classOf[MagentoGoController])


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

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(registerView, Some(RequestExpiredException()))
        } else if (bindingResult.hasErrors) {
            error(registerView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            magentoGoPartnerServiceManager.registerPartner(registerForm.createPartner).onComplete(_.value.get.fold(
                e => error(registerView, Some(e)),
                _ match {
                    case RegisterMagentoGoPartnerResponse(_, Left(e)) => error(registerView, Some(e))
                    case RegisterMagentoGoPartnerResponse(_, Right(result)) =>
                        logger.debug("Successfully registered MagentoGo partner {}", result.partner.name)
                        val modelAndView = new ModelAndView(postRegisterView)
                        modelAndView.addObject("partner", result.partner)
                        modelAndView.addObject("partnerUser", result.partnerUser)
                        modelAndView.addObject("magentoGoPartner", result.magentoGoPartner)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume
                }))

            continuation.undispatch
        })
    }

}
