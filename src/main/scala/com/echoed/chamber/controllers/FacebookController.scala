package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator,LocateWithFacebookServiceResponse,LocateWithIdResponse,GetEchoedUserResponse,AssignFacebookServiceResponse}
import com.echoed.util.CookieManager
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import com.echoed.chamber.services.echo.EchoService
import java.util.{HashMap => JMap}
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import akka.actor.Actor
import com.echoed.chamber.services.facebook.{LocateByCodeResponse, LocateByIdResponse, FacebookService, FacebookServiceLocator}


@Controller
@RequestMapping(Array("/facebook"))
class FacebookController {

    private val logger = LoggerFactory.getLogger(classOf[FacebookController])

    @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoService: EchoService = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var facebookLoginErrorView: String = _
    @BeanProperty var echoView: String = _
    @BeanProperty var dashboardView: String = _

    @RequestMapping(value = Array("/add"), method = Array(RequestMethod.GET))
    def add(
            @RequestParam("code") code:String,
            @RequestParam(value="redirect", required = false) redirect: String,
            @CookieValue(value="echoedUserId", required= true) echoedUserId: String,
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) {
            logger.error("Unexpected error encountered during Facebook add with code %s and redirect {}" format(code, redirect), e)
            val modelAndView = new ModelAndView(facebookLoginErrorView, "errorMessage", e.getMessage)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume
            modelAndView
        }

        if(Option(code) == None || continuation.isExpired){
            error(RequestExpiredException("We encountered an error talking to Facebook"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {

            continuation.suspend(httpServletResponse)

            val parameters = new JMap[String, Array[String]]()
            parameters.putAll(httpServletRequest.getParameterMap)
            parameters.remove("code")

            val redirectView = "redirect:http://v1-api.echoed.com/" + redirect

            logger.debug("Redirect View {}" , redirectView)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case LocateWithIdResponse(_, Left(e)) => error(e)
                    case LocateWithIdResponse(_, Right(eus)) => eus.getEchoedUser.onComplete(_.value.get.fold(
                        error(_),
                        _ match {
                            case GetEchoedUserResponse(_, Left(e)) => error(e)
                            case GetEchoedUserResponse(_, Right(echoedUser)) => Option(echoedUser.facebookUserId).cata(
                                facebookUserId => {
                                    logger.debug("Facebook account already attached {}", echoedUser.facebookUserId)
                                    val modelAndView = new ModelAndView(redirectView);
                                    continuation.setAttribute("modelAndView",modelAndView)
                                    continuation.resume
                                },
                                {
                                    logger.debug("No Existing Facebook User Id ")

                                    val queryString = "/facebook/add?" + {
                                        val index = httpServletRequest.getQueryString.indexOf("&code=")
                                        if (index > -1) {
                                            httpServletRequest.getQueryString.substring(0, index)
                                        } else {
                                            httpServletRequest.getQueryString
                                        }
                                    }

                                    val futureFacebookService = facebookServiceLocator.locateByCode(code, queryString)
                                    futureFacebookService.onComplete(_.value.get.fold(
                                        error(_),
                                        _ match {
                                            case LocateByCodeResponse(_, Left(e)) => error(e)
                                            case LocateByCodeResponse(_, Right(facebookService)) =>
                                                eus.assignFacebookService(facebookService).onComplete(_.value.get.fold(
                                                    error(_),
                                                    _ match {
                                                        case AssignFacebookServiceResponse(_, Left(error)) =>
                                                            logger.debug("Error assigning Facebook service to EchoedUser {}: {}", echoedUser.id, error.message)
                                                            val modelAndView = new ModelAndView(redirectView);
                                                            modelAndView.addAllObjects(parameters)
                                                            modelAndView.addObject("error", error.getMessage)
                                                            continuation.setAttribute("modelAndView", modelAndView)
                                                            continuation.resume

                                                        case AssignFacebookServiceResponse(_, Right(fs)) =>
                                                            val modelAndView = new ModelAndView(redirectView);
                                                            modelAndView.addAllObjects(parameters)
                                                            continuation.setAttribute("modelAndView", modelAndView)
                                                            continuation.resume
                                                    }))
                                        }))
                                })
                        }))
                }))

            continuation.undispatch()
        }
    }


    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam("code") code: String,
            @RequestParam(value = "redirect", required = false) redirect: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) {
            logger.error("Unexpected error encountered during Facebook login with code %s and redirect {}" format(code, redirect), e)
            val modelAndView = new ModelAndView(facebookLoginErrorView, "errorMessage", e.getMessage)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume
            modelAndView
        }

        if (Option(code) == None || continuation.isExpired) {
            error(RequestExpiredException("We encountered an error talking to Facebook"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            logger.debug("Requesting FacebookService with code {}", code)

            val queryString = {
                val index = httpServletRequest.getQueryString.indexOf("&code=")
                "/facebook/login?" + httpServletRequest.getQueryString.substring(0, index)
            }

            facebookServiceLocator.locateByCode(code, queryString).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case LocateByCodeResponse(_, Left(e)) => error(e)
                    case LocateByCodeResponse(_, Right(facebookService)) =>
                        logger.debug("Received FacebookService, fetching EchoedUserService with code {}", code)
                        echoedUserServiceLocator.getEchoedUserServiceWithFacebookService(facebookService).onComplete(_.value.get.fold(
                            error(_),
                            _ match {
                                case LocateWithFacebookServiceResponse(_, Left(e)) => error(e)
                                case LocateWithFacebookServiceResponse(_, Right(s))=> s.getEchoedUser.onComplete(_.value.get.fold(
                                    error(_),
                                    _ match {
                                        case GetEchoedUserResponse(_, Left(e)) => error(e)
                                        case GetEchoedUserResponse(_, Right(echoedUser))=>
                                            cookieManager.addCookie(httpServletResponse, "echoedUserId", echoedUser.id)
                                            val redirectView = "redirect:http://v1-api.echoed.com/" + redirect;
                                            logger.debug("Redirecting to View: {} ", redirectView);
                                            val modelAndView = new ModelAndView(redirectView);
                                            continuation.setAttribute("modelAndView", modelAndView)
                                            continuation.resume()
                                    }))
                            }))
                }))

            continuation.undispatch
        })

    }

}

