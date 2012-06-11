package com.echoed.chamber.controllers.echo

import scala.Array
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import reflect.BeanProperty
import com.echoed.chamber.services.partner.PartnerServiceManager
import com.echoed.chamber.controllers.CookieManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import javax.validation.Valid
import org.springframework.validation.{BindingResult, Validator }
import org.springframework.web.bind.annotation.{InitBinder, RequestParam, RequestMethod, RequestMapping}
import org.springframework.web.bind.WebDataBinder
import org.springframework.core.convert.ConversionService
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.echoeduser.{EchoedUserServiceLocator, EmailAlreadyExists}

import org.springframework.web.context.request.async.DeferredResult


@Controller
class EchoRegisterController {

    @BeanProperty var partnerServiceManager: PartnerServiceManager = _
    @BeanProperty var echoRegisterView: String = _

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var errorView: String = _
    @BeanProperty var globalValidator: Validator = _
    @BeanProperty var formValidator: Validator = _
    @BeanProperty var conversionService: ConversionService = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var echoConfirmViewUrl: String = _
    @BeanProperty var echoEchoedViewUrl: String = _
    @BeanProperty var echoCloseViewUrl: String = _

    private final val logger = LoggerFactory.getLogger(classOf[EchoRegisterController])

    @InitBinder
    def initBinder(binder: WebDataBinder) {
        binder.setFieldDefaultPrefix("registerForm.")
        binder.setValidator(globalValidator)
        binder.setConversionService(conversionService)
    }


    @RequestMapping(value = Array("/echo/register"), method = Array(RequestMethod.GET))
    def details(
                   @RequestParam(value = "id", required = true) id: String,
                   @RequestParam(value= "close", required = false) close: Boolean,
                   httpServletRequest: HttpServletRequest,
                   httpServletResponse: HttpServletResponse) = {

        val modelAndView = new ModelAndView(echoRegisterView)
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
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val errorModelAndView = new ModelAndView(echoRegisterView)
        if (close == true) errorModelAndView.addObject("close", "true")
        errorModelAndView.addObject("id", id)

        if (!bindingResult.hasErrors) {
            formValidator.validate(echoRegisterForm, bindingResult)
        }

        if (bindingResult.hasErrors){
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            val eu = cookieManager.findEchoedUserCookie(httpServletRequest)

            echoedUserServiceLocator.getEchoedUserServiceWithId(eu.get)
                .flatMap(_.resultOrException.updateEchoedUserEmail(echoRegisterForm.getEmail))
                .foreach(_.cata(
                    _ match {
                        case EmailAlreadyExists(em, _ , _) =>
                            logger.debug("Email already exists: {}", em)
                            val modelAndView = new ModelAndView(echoRegisterView)
                            modelAndView.addObject("id", id)
                            if (close == true) modelAndView.addObject("close", "true")
                            modelAndView.addObject("error_email", "This Email Already Exists")
                            result.set(modelAndView)
                    },
                    _ => {
                        if (close) result.set(new ModelAndView(echoCloseViewUrl, "id", id))
                        else result.set(new ModelAndView(echoEchoedViewUrl, "id", id))
                    }))

            result
        }
    }

}