package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import com.echoed.chamber.services.echoeduser.{EchoedUserServiceLocator,LocateWithFacebookServiceResponse,LocateWithIdResponse,GetEchoedUserResponse,AssignFacebookServiceResponse}
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import com.echoed.chamber.services.echo.EchoService
import java.util.{Map => JMap, HashMap => JHashMap}
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import java.net.URLEncoder
import org.apache.commons.codec.binary.Base64
import com.echoed.util.ScalaObjectMapper
import org.eclipse.jetty.continuation.{Continuation, ContinuationSupport}
import com.echoed.chamber.services.facebook.{LocateByFacebookIdResponse, FacebookService, LocateByCodeResponse, FacebookServiceLocator}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.echoed.chamber.services.EchoedException
import org.springframework.validation.ObjectError


@Controller
@RequestMapping(Array("/facebook"))
class FacebookController extends NetworkController {

    private val logger = LoggerFactory.getLogger(classOf[FacebookController])

    @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var facebookClientId: String = _
    @BeanProperty var facebookClientSecret: String = _
    @BeanProperty var facebookCanvasApp: String = _

    @BeanProperty var postAddView: String = _
    @BeanProperty var postLoginView: String = _
    @BeanProperty var facebookLoginErrorView: String = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var siteUrl: String = _

    @BeanProperty var permissions = "email,publish_stream,read_stream,offline_access,publish_actions"
    @BeanProperty var authUrl = "https://www.facebook.com/dialog/oauth?scope=%s&client_id=%s&redirect_uri=%s"

    private var facebookClientSecretBytes: Array[Byte] = _
    def init() {
        facebookClientSecretBytes = facebookClientSecret.getBytes("UTF-8")
    }

    @RequestMapping(value = Array("/add"), method = Array(RequestMethod.GET))
    def add(
            @RequestParam("code") code:String,
            @RequestParam(value="redirect", required = false) redirect: String,
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) {
            logger.error("Unexpected error encountered during Facebook add with code %s and redirect {}" format(code, redirect), e)
            val modelAndView = new ModelAndView(facebookLoginErrorView) with Errors
            modelAndView.addError(e)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume
            modelAndView
        }

        if (Option(code) == None || continuation.isExpired) {
            error(RequestExpiredException("We encountered an error talking to Facebook"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {

            continuation.suspend(httpServletResponse)

            val parameters = new JHashMap[String, Array[String]]()
            parameters.putAll(httpServletRequest.getParameterMap.asInstanceOf[JMap[String, Array[String]]])
            parameters.remove("code")

            val redirectView = "%s/%s" format(postAddView, Option(redirect).getOrElse(""))

            logger.debug("Redirect View {}" , redirectView)

            val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId.get).onComplete(_.value.get.fold(
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
                                    val modelAndView = new ModelAndView(redirectView) with Errors
                                    modelAndView.addError("Facebook account already in use")
                                    continuation.setAttribute("modelAndView", modelAndView)
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
                                                            val modelAndView = new ModelAndView(redirectView) with Errors;
                                                            modelAndView.addError(error)
                                                            modelAndView.addAllObjects(parameters)
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
            val modelAndView = new ModelAndView(facebookLoginErrorView) with Errors
            modelAndView.addError(e)
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


            facebookServiceLocator.locateByCode(code, queryString).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case LocateByCodeResponse(_, Left(e)) => error(e)
                    case LocateByCodeResponse(_, Right(facebookService)) => finishLogin(
                            httpServletResponse,
                            error _,
                            redirect,
                            continuation,
                            facebookService,
                            httpServletRequest)
                }))

            continuation.undispatch
        })

    }

    private def finishLogin(
            httpServletResponse: HttpServletResponse,
            error: Throwable => ModelAndView,
            redirect: String,
            continuation: Continuation,
            facebookService: FacebookService,
            httpServletRequest: HttpServletRequest) = {
        echoedUserServiceLocator.getEchoedUserServiceWithFacebookService(facebookService).onComplete(_.value.get.fold(
            error(_),
            _ match {
                case LocateWithFacebookServiceResponse(_, Left(e)) => error(e)
                case LocateWithFacebookServiceResponse(_, Right(s))=> s.getEchoedUser.onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case GetEchoedUserResponse(_, Left(e)) => error(e)
                        case GetEchoedUserResponse(_, Right(echoedUser))=>
                            cookieManager.addEchoedUserCookie(httpServletResponse, echoedUser, httpServletRequest)
                            val redirectView = "%s/%s" format(postLoginView, Option(redirect).getOrElse(""))
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
            val modelAndView = new ModelAndView(facebookLoginErrorView) with Errors
            modelAndView.addError(e)
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
            mac.init(new SecretKeySpec(facebookClientSecretBytes, algorithm))
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
                                    facebookService,
                                    httpServletRequest)
                        }))
                },
                {
                    val canvasPage = URLEncoder.encode(facebookCanvasApp, "UTF-8")
                    continuation.setAttribute(
                            "modelAndView",
                            new ModelAndView(
                                    "authredirect",
                                    "authUrl",
                                    authUrl format(permissions, facebookClientId, canvasPage)))
                    continuation.resume()
                })

            continuation.undispatch()
        })
    }

    def makeAuthorizeUrl(postAuthorizeUrl: String, add: Boolean = false) = {
        val postUrl = "%s/facebook/%s?redirect=%s" format (
                siteUrl,
                if (add) "add" else "login",
                URLEncoder.encode(postAuthorizeUrl, "UTF-8"))

        authUrl format(
                permissions,
                facebookClientId,
                URLEncoder.encode(postUrl, "UTF-8"))
    }

}

