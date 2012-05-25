package com.echoed.chamber.controllers.echo

import scala.Array
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.controllers.ControllerUtils._
import reflect.BeanProperty
import com.echoed.chamber.services.partner.{PartnerServiceManager, LocateByEchoIdResponse}
import com.echoed.chamber.controllers.{CookieManager, RequestExpiredException}
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import javax.validation.Valid
import org.springframework.validation.{BindingResult, Validator }
import org.springframework.web.bind.annotation.{InitBinder, RequestParam, RequestMethod, RequestMapping}
import org.springframework.web.bind.WebDataBinder
import org.springframework.core.convert.ConversionService
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.echoeduser.{UpdateEchoedUserEmailResponse, LocateWithIdResponse, EchoedUserServiceLocator, EchoedUserNotFound, EmailAlreadyExists}

import scalaz._
import Scalaz._


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

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        val eu = cookieManager.findEchoedUserCookie(httpServletRequest)

        if (eu.isEmpty){
            error(errorView, EchoedUserNotFound(""))
        } else if (continuation.isExpired){
            error(errorView, RequestExpiredException())
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            val ec = cookieManager.findEchoClickCookie(httpServletRequest)

            logger.debug("Locating Partner By EchoId: {}", id)

            partnerServiceManager.locatePartnerByEchoId(id).onComplete(_.value.get.fold(
                e => error(errorView, e),
                _ match {
                    case LocateByEchoIdResponse(_, Left(e)) => throw e
                    case LocateByEchoIdResponse(_, Right(ps)) =>
                        ps.recordEchoStep(id, "register", eu, ec)
                        val modelAndView = new ModelAndView(echoRegisterView)
                        modelAndView.addObject("id",id)
                        if(close == true)
                            modelAndView.addObject("close","true")
                        continuation.setAttribute("modelAndView",modelAndView)
                        continuation.resume()
                }))
            continuation.undispatch()
        }
    }

    @RequestMapping(value = Array("echo/register"), method = Array(RequestMethod.POST))
    def detailsPost(
            @RequestParam(value = "id", required= true) id: String,
            @RequestParam(value= "close", required = false) close: Boolean,
            @Valid echoRegisterForm: EchoRegisterForm,
            bindingResult: BindingResult,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        if (!bindingResult.hasErrors) {
            formValidator.validate(echoRegisterForm, bindingResult)
        }

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        val eu = cookieManager.findEchoedUserCookie(httpServletRequest)

        logger.debug("Echo Register Post: {} ", id)

        if (eu.isEmpty) {
            error(errorView, EchoedUserNotFound("Echoed User Not Found"))
        } else if (continuation.isExpired) {
            error(errorView, RequestExpiredException("Request Expired"))
        } else if (bindingResult.hasErrors){
            val modelAndView = new ModelAndView(echoRegisterView)
            if(close == true) modelAndView.addObject("close", "true")
            modelAndView.addObject("id", id)
            modelAndView
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            val ec = cookieManager.findEchoClickCookie(httpServletRequest)

            echoRegisterForm.getEmail match {
                case email =>
                    //Update the Email Address for EchoedUser
                    val echoedUserResponse = echoedUserServiceLocator.getEchoedUserServiceWithId(eu.get).flatMap(_.resultOrException.updateEchoedUserEmail(email))

                    //Record Register Success Step
                    val recordEchoStepResponse = partnerServiceManager.locatePartnerByEchoId(id).flatMap(_ match {
                        case LocateByEchoIdResponse(_, Left(e)) => throw e
                        case LocateByEchoIdResponse(_, Right(ps)) => ps.recordEchoStep(id, "register-success", eu, ec)
                    })

                    (for {
                        eur <- echoedUserResponse
                        resr <- recordEchoStepResponse
                    }  yield {
                        val es = resr.resultOrException
                        eur.cata(
                            _ match {
                                case EmailAlreadyExists(em, _ , _) =>
                                    logger.debug("Email Already Exists")
                                    val modelAndView = new ModelAndView(echoRegisterView)
                                    modelAndView.addObject("id", id)
                                    if(close == true) modelAndView.addObject("close", "true")
                                    modelAndView.addObject("error_email", "This Email Already Exists")
                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume()
                            },
                            _ match {
                                case e =>
                                    close match {
                                        case true =>
                                            val modelAndView = new ModelAndView(echoCloseViewUrl, "id", id)
                                            continuation.setAttribute("modelAndView", modelAndView)
                                            continuation.resume()
                                        case false =>
                                            val modelAndView = new ModelAndView(echoEchoedViewUrl, "id", id)
                                            continuation.setAttribute("modelAndView", modelAndView)
                                            continuation.resume()
                                    }
                            }
                        )
                    }).onException { case e => error(errorView, e)}
            }
            continuation.undispatch()
        }
    }
}