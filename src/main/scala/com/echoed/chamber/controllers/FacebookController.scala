package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import com.echoed.chamber.services.echoeduser.{EchoedUserServiceLocator,LocateWithFacebookServiceResponse,LocateWithIdResponse,GetEchoedUserResponse,AssignFacebookServiceResponse}
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import com.echoed.chamber.services.echo.EchoService
import java.util.{HashMap => JMap}
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import java.net.URLEncoder
import org.apache.commons.codec.binary.Base64
import com.echoed.util.{ScalaObjectMapper, CookieManager}
import org.eclipse.jetty.continuation.{Continuation, ContinuationSupport}
import com.echoed.chamber.services.facebook.{LocateByFacebookIdResponse, FacebookService, LocateByCodeResponse, FacebookServiceLocator}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


@Controller
@RequestMapping(Array("/facebook"))
class FacebookController {

    private val logger = LoggerFactory.getLogger(classOf[FacebookController])

    @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _


    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var facebookLoginErrorView: String = _

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

        if (Option(code) == None || continuation.isExpired) {
            error(RequestExpiredException("We encountered an error talking to Facebook"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {

            continuation.suspend(httpServletResponse)

            val parameters = new JMap[String, Array[String]]()
            parameters.putAll(httpServletRequest.getParameterMap)
            parameters.remove("code")

            val redirectView = "redirect:http://www.echoed.com/" + Option(redirect).getOrElse("")

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
                                    logger.error("Facebook account already attached {}", echoedUser.facebookUserId)
                                    val modelAndView = new ModelAndView(redirectView);
                                    continuation.setAttribute("modelAndView",modelAndView)
                                    continuation.resume
                                },
                                {
                                    logger.debug("No Existing Facebook User Id ")

                                    val queryString = "/facebook/add" + {
                                        val index = httpServletRequest.getQueryString.indexOf("&code=")
                                        if (index > -1) {
                                            "?" + httpServletRequest.getQueryString.substring(0, index)
                                        } else {
                                            "" //httpServletRequest.getQueryString
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

        def error(e: Throwable) = {
            logger.error("Unexpected error encountered during Facebook login with code %s and redirect %s" format(code, redirect), e)
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

            val index = httpServletRequest.getQueryString.indexOf("&code=")
            val queryString = "/facebook/login" +
                (if (index > -1) "?" + httpServletRequest.getQueryString.substring(0, index) else "")
//            httpServletRequest.getQueryString.indexOf("&code=")
//                val index = httpServletRequest.getQueryString.indexOf("&code=")
//                "/facebook/login?" + httpServletRequest.getQueryString.substring(0, index)
//            }

            facebookServiceLocator.locateByCode(code, queryString).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case LocateByCodeResponse(_, Left(e)) => error(e)
                    case LocateByCodeResponse(_, Right(facebookService)) => finishLogin(
                            httpServletResponse,
                            error _,
                            redirect,
                            continuation,
                            facebookService)
                }))

            continuation.undispatch
        })

    }

    private def finishLogin(
            httpServletResponse: HttpServletResponse,
            error: Throwable => ModelAndView,
            redirect: String,
            continuation: Continuation,
            facebookService: FacebookService) = {
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
                            val redirectView = "redirect:http://www.echoed.com/" + Option(redirect).getOrElse("");
                            logger.debug("Redirecting to View: {} ", redirectView);
                            val modelAndView = new ModelAndView(redirectView);
                            continuation.setAttribute("modelAndView", modelAndView)
                            continuation.resume()
                    }))
            }))
    }


    @RequestMapping(value = Array("/app"), method = Array(RequestMethod.POST))
    def app(
            @RequestParam(value = "signed_request", required = true) signedRequest: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) = {
            logger.error("Unexpected error encountered during Facebook app login with signed request %s " format signedRequest, e)
            val modelAndView = new ModelAndView(facebookLoginErrorView, "errorMessage", e.getMessage)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume
            modelAndView
        }

        if (continuation.isExpired) {
            error(RequestExpiredException("We encountered an error talking to Facebook"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            val parts = signedRequest.split("\\.")
            val encodedSig = parts(0)
            val encodedPayload = parts(1)

            val payload = new ScalaObjectMapper().readTree(new Base64(true).decode(encodedPayload))

            val algorithm = payload.get("algorithm").asText().replace("-", "")
            val mac = Mac.getInstance(algorithm)
            //TODO externalize the app secret!
            mac.init(new SecretKeySpec("32dc29f669ce9f97bc9bade3bdf1ca79".getBytes, algorithm))
            val encodedExpectedSig = Base64.encodeBase64URLSafeString(mac.doFinal(encodedPayload.getBytes))

            logger.debug("sig {}", encodedSig)
            logger.debug("sig {}", encodedExpectedSig)

            if (encodedExpectedSig != encodedSig) {
                throw new IllegalArgumentException("Invalid signature")
            }

            Option(payload.get("user_id")).map(_.asText()).cata(
                facebookId => {
                    val accessToken = payload.get("oauth_token").asText()
                    facebookServiceLocator.locateByFacebookId(facebookId, accessToken).onComplete(_.value.get.fold(
                        error(_),
                        _ match {
                            case LocateByFacebookIdResponse(_, Left(e)) => error(e)
                            case LocateByFacebookIdResponse(_, Right(facebookService)) => finishLogin(
                                    httpServletResponse,
                                    error _,
                                    "?app=facebook",
                                    continuation,
                                    facebookService)
                        }))
                },
                {
                    val appId = "177687295582534" //TODO externalize this!
                    val canvasPage = URLEncoder.encode("http://apps.facebook.com/echoedapp/", "UTF-8")
                    val authUrl = "https://www.facebook.com/dialog/oauth?client_id=%s&redirect_uri=%s" format(appId, canvasPage)
                    continuation.setAttribute("modelAndView", new ModelAndView("view.authredirect", "authUrl", authUrl))
                    continuation.resume()
                })

            continuation.undispatch()
        })
    }

}

