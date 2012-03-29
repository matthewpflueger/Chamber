package com.echoed.chamber.controllers.shopify

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.WebDataBinder
import com.echoed.chamber.controllers.{RequestExpiredException, CookieManager}
import javax.validation.Valid
import org.springframework.validation.{Validator, BindingResult}
import org.springframework.core.convert.ConversionService
import com.echoed.chamber.controllers.ControllerUtils.error
import org.springframework.web.bind.annotation.{RequestParam, InitBinder, RequestMapping, RequestMethod}
import com.echoed.chamber.services.partner._
import com.echoed.chamber.services.shopify.ShopifyUserServiceLocator


@Controller
class ShopifyRegisterController {

    private val logger = LoggerFactory.getLogger(classOf[ShopifyRegisterController])

    @BeanProperty var partnerServiceManager: PartnerServiceManager = _
    @BeanProperty var shopifyUserServiceLocator: ShopifyUserServiceLocator = _

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

    @RequestMapping(value = Array("/shopify/update"), method = Array(RequestMethod.GET))
    def updateGet(
                       @RequestParam(value = "pid", required = true) pid: String,
                       httpServletRequest: HttpServletRequest,
                       httpServletResponse: HttpServletResponse) = {

        implicit  val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) = {
            val modelAndView = new ModelAndView("ERROR")
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume()
            modelAndView
        }

        logger.debug("Partner Id: {}", pid)

        if (continuation.isExpired) {
            error(RequestExpiredException("Error"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            partnerServiceManager.locatePartnerService(pid).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case LocateResponse(_, Left(e: PartnerNotActive)) =>
                        continuation.setAttribute("modelAndView", new ModelAndView(registerView, "registerForm", new ShopifyRegisterForm(Some(pid))))
                        continuation.resume()
                    case LocateResponse(_, Right(partnerService)) =>
                        logger.debug("partnerService Received: {}", partnerService)
                        continuation.setAttribute("modelAndView", new ModelAndView(registerView, "registerForm", new ShopifyRegisterForm(Some(pid))))
                        continuation.resume()
                }
            ))
            continuation.undispatch()
        })
    }

    @RequestMapping(value = Array("/shopify/update"), method = Array(RequestMethod.POST))
    def updatePost(
                        @Valid registerForm: ShopifyRegisterForm,
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

            registerForm.updatePartnerSettings(partnerServiceManager.updatePartnerSettings(_)).onComplete(_.value.get.fold(
                e => error(registerView, Some(e)),
                _ match {
                    case UpdatePartnerSettingsResponse(_, Left(e)) => error(registerView, Some(e))
                    case UpdatePartnerSettingsResponse(_, Right(partnerSettings)) =>
                        logger.debug("Successfully update partner settings: {}", partnerSettings)
                        continuation.setAttribute("modelAndView", new ModelAndView("redirect:http://www.echoed.com"))
                        continuation.resume()
                }
            ))
            continuation.undispatch()
        })

    }

}
