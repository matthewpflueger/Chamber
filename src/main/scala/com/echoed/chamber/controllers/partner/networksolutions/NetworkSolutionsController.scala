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
import com.echoed.chamber.controllers.RequestExpiredException
import com.echoed.chamber.controllers.ControllerUtils.error
import com.echoed.chamber.services.partner.networksolutions.{AuthNetworkSolutionsPartnerResponse, RegisterNetworkSolutionsPartnerResponse, NetworkSolutionsPartnerServiceManager}


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

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(registerView, Some(RequestExpiredException()))
        } else if (bindingResult.hasErrors) {
            error(registerView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            networkSolutionsPartnerServiceManager.registerPartner(
                    registerForm.name,
                    registerForm.email,
                    registerForm.phone,
                    successUrl).onComplete(_.value.get.fold(
                e => error(registerView, Some(e)),
                _ match {
                    case RegisterNetworkSolutionsPartnerResponse(_, Left(e)) => error(registerView, Some(e))
                    case RegisterNetworkSolutionsPartnerResponse(_, Right(loginUrl)) =>
                        logger.debug("Successfully registered Network Solutions partner {}, login url is {}", registerForm.email, loginUrl)
                        continuation.setAttribute("modelAndView", new ModelAndView("redirect:%s" format loginUrl))
                        continuation.resume
                }))

            continuation.undispatch
        })

    }

    @RequestMapping(value = Array("/auth"), method = Array(RequestMethod.GET))
    def auth(
            @RequestParam(value = "userkey", required = true) userKey: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(registerView, Some(RequestExpiredException()))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            logger.debug("Attempting to authorize Network Solutions partner {}", userKey)
            networkSolutionsPartnerServiceManager.authPartner(userKey).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case AuthNetworkSolutionsPartnerResponse(_, Left(e)) => error(e)
                    case AuthNetworkSolutionsPartnerResponse(_, Right(envelope)) =>
                        logger.debug("Successfully authorized {}", envelope.networkSolutionsPartner)
                        val modelAndView = new ModelAndView(postAuthView)
                        modelAndView.addObject("networkSolutionsPartner", envelope.networkSolutionsPartner)
                        modelAndView.addObject("partnerUser", envelope.partnerUser)
                        modelAndView.addObject("partner", envelope.partner)

                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume

                }))

            continuation.undispatch()
        }
    }


}
