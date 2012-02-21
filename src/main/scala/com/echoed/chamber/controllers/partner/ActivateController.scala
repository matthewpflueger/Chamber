package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.springframework.web.bind.WebDataBinder
import com.echoed.chamber.controllers.{RequestExpiredException, CookieManager}
import javax.validation.Valid
import org.springframework.validation.{Validator, BindingResult}
import org.springframework.core.convert.ConversionService
import org.springframework.web.bind.annotation.{PathVariable, InitBinder, RequestMapping, RequestMethod}
import com.echoed.chamber.services.partneruser._
import com.echoed.util.{ScalaObjectMapper, Encrypter}
import com.echoed.chamber.controllers.ControllerUtils.error
import org.springframework.web.servlet.ModelAndView


@Controller
class ActivateController {

    private val logger = LoggerFactory.getLogger(classOf[ActivateController])

    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _
    @BeanProperty var encrypter: Encrypter = _

    @BeanProperty var activateView: String = _
    @BeanProperty var postActivateView: String = _
    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var globalValidator: Validator = _
    @BeanProperty var formValidator: Validator = _
    @BeanProperty var conversionService: ConversionService = _

    @InitBinder
    def initBinder(binder: WebDataBinder) {
        binder.setFieldDefaultPrefix("activateForm.")
        binder.setValidator(globalValidator)
        binder.setConversionService(conversionService)
    }

    @RequestMapping(value = Array("/partner/activate/{code}"), method = Array(RequestMethod.GET))
    def activateGet(
            @PathVariable("code") code: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(activateView, Some(RequestExpiredException()))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            logger.debug("Starting activation with code {}", code)

            val payload = new ScalaObjectMapper().readTree(encrypter.decrypt(code))

            val email = Option(payload.get("email")).map(_.asText()).orNull
            val password = Option(payload.get("password")).map(_.asText()).orNull

            logger.debug("Starting activation for {}", email)

            partnerUserServiceLocator.login(email, password).onComplete(_.value.get.fold(
                e => error(activateView, Some(e)),
                _ match {
                    case LoginResponse(_, Left(e)) => error(activateView, Some(e))
                    case LoginResponse(_, Right(partnerUserService)) => partnerUserService.getPartnerUser.onComplete(_.value.get.fold(
                        e => error(activateView, Some(e)),
                        _ match {
                            case GetPartnerUserResponse(_, Left(e)) => error(activateView, Some(e))
                            case GetPartnerUserResponse(_, Right(partnerUser)) =>
                                val modelAndView = new ModelAndView(activateView)
                                modelAndView.addObject("partnerUser", partnerUser)
                                modelAndView.addObject("activateForm", new ActivateForm(partnerUser.id))
                                continuation.setAttribute("modelAndView", modelAndView)
                                continuation.resume
                                logger.debug("Showing activation form for partner user {}: {}", partnerUser.id, partnerUser.name)
                        }))
                }))

            continuation.undispatch()
        })
    }

    @RequestMapping(value = Array("/partner/activate"), method = Array(RequestMethod.POST))
    def activatePost(
            @Valid activateForm: ActivateForm,
            bindingResult: BindingResult,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {


        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (!bindingResult.hasErrors) {
            formValidator.validate(activateForm, bindingResult)
        }

        if (continuation.isExpired) {
            error(activateView, Some(RequestExpiredException()))
        } else if (bindingResult.hasErrors) {
            error(activateView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            logger.debug("Activating partner user {}", activateForm.partnerUserId)

            partnerUserServiceLocator.locate(activateForm.partnerUserId).onComplete(_.value.get.fold(
                e => error(activateView, Some(e)),
                _ match {
                    case LocateResponse(_, Left(e)) => error(activateView, Some(e))
                    case LocateResponse(_, Right(partnerUserService)) => partnerUserService.activate(activateForm.password).onComplete(_.value.get.fold(
                        e => error(activateView, Some(e)),
                        _ match {
                            case ActivatePartnerUserResponse(_, Left(e: InvalidPassword)) =>
                                bindingResult.rejectValue("password", e.code.get, e.message)
                                error(activateView)
                            case ActivatePartnerUserResponse(_, Right(partnerUser)) =>
                                cookieManager.addPartnerUserCookie(httpServletResponse, partnerUser, httpServletRequest)
                                val modelAndView = new ModelAndView(postActivateView)
                                modelAndView.addObject("partnerUser", partnerUser)
                                continuation.setAttribute("modelAndView", modelAndView)
                                continuation.resume
                                logger.debug("Activated partner user {}: {}", partnerUser.id, partnerUser.name)
                            case ActivatePartnerUserResponse(_, Left(e)) => error(activateView, Some(e))
                        }))
                }))

            continuation.undispatch()
        })

    }

}
