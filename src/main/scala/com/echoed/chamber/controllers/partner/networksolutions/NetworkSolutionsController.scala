package com.echoed.chamber.controllers.partner.networksolutions

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
import com.echoed.chamber.services.partner.networksolutions.{AuthNetworkSolutionsPartnerResponse, RegisterNetworkSolutionsPartnerResponse, NetworkSolutionsPartnerServiceManager}
import com.echoed.chamber.controllers.{Errors, RequestExpiredException}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.partner.magentogo.RegisterMagentoGoPartnerResponse


@Controller
@RequestMapping(Array("/networksolutions"))
class NetworkSolutionsController {

    @BeanProperty var networkSolutionsPartnerServiceManager: NetworkSolutionsPartnerServiceManager = _

    @BeanProperty var registerView: String = _
    @BeanProperty var postAuthView: String = _
    @BeanProperty var globalValidator: Validator = _
    @BeanProperty var conversionService: ConversionService = _

    @BeanProperty var successUrl: String = _

    private val logger = LoggerFactory.getLogger(classOf[NetworkSolutionsController])


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

            networkSolutionsPartnerServiceManager.registerPartner(
                    registerForm.name,
                    registerForm.email,
                    registerForm.phone,
                    successUrl).onComplete(_.fold(
                e => {
                    errorModelAndView.addError(e)
                    result.set(errorModelAndView)
                },
                _ match {
                    case RegisterNetworkSolutionsPartnerResponse(_, Left(e)) =>
                        errorModelAndView.addError(e)
                        result.set(errorModelAndView)
                    case RegisterNetworkSolutionsPartnerResponse(_, Right(loginUrl)) =>
                        logger.debug("Successfully registered Network Solutions partner {}, login url is {}", registerForm.email, loginUrl)
                        result.set(new ModelAndView("redirect:%s" format loginUrl))
                }))

            result
        }
    }


    @RequestMapping(value = Array("/auth"), method = Array(RequestMethod.GET))
    def auth(
            @RequestParam(value = "userkey", required = true) userKey: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView("error"))

        logger.debug("Attempting to authorize Network Solutions partner {}", userKey)
        networkSolutionsPartnerServiceManager.authPartner(userKey).onSuccess {
            case AuthNetworkSolutionsPartnerResponse(_, Right(envelope)) =>
                logger.debug("Successfully authorized {}", envelope.networkSolutionsPartner)
                val modelAndView = new ModelAndView(postAuthView)
                modelAndView.addObject("networkSolutionsPartner", envelope.networkSolutionsPartner)
                modelAndView.addObject("partnerUser", envelope.partnerUser)
                modelAndView.addObject("partner", envelope.partner)
                result.set(modelAndView)
        }

        result
    }

}
