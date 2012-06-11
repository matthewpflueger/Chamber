package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import com.echoed.chamber.services.echoeduser.{EchoedUserServiceLocator,LocateWithFacebookServiceResponse,LocateWithIdResponse,GetEchoedUserResponse,AssignFacebookServiceResponse}
import org.springframework.web.bind.annotation.{RequestParam, RequestMapping, RequestMethod}
import java.util.{Map => JMap, HashMap => JHashMap}
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import java.net.URLEncoder
import org.apache.commons.codec.binary.Base64
import com.echoed.util.ScalaObjectMapper
import com.echoed.chamber.services.facebook.{LocateByFacebookIdResponse, FacebookService, LocateByCodeResponse, FacebookServiceLocator}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.springframework.web.context.request.async.DeferredResult


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

    @BeanProperty var extendedPermissions = "email,user_birthday,publish_actions,publish_stream,read_stream"
    @BeanProperty var limitedPermissions = "email,user_birthday,publish_actions"
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

        val errorModelAndView = new ModelAndView(facebookLoginErrorView) with Errors
        val result = new DeferredResult(errorModelAndView)

        def error(e: Throwable) {
            logger.error("Unexpected error encountered during Facebook add with code %s and redirect {}" format(code, redirect), e)
            val modelAndView = new ModelAndView(facebookLoginErrorView) with Errors
            modelAndView.addError(e)
            result.set(modelAndView)
        }

        val parameters = new JHashMap[String, Array[String]]()
        parameters.putAll(httpServletRequest.getParameterMap.asInstanceOf[JMap[String, Array[String]]])
        parameters.remove("code")

        val redirectView = "%s/%s" format(postAddView, Option(redirect).getOrElse(""))

        logger.debug("Redirect View {}" , redirectView)

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest)

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId.get).onSuccess {
            case LocateWithIdResponse(_, Right(eus)) => eus.getEchoedUser.onSuccess {
                case GetEchoedUserResponse(_, Right(echoedUser)) => Option(echoedUser.facebookUserId).cata(
                    facebookUserId => {
                        logger.error("Facebook account already attached {}", echoedUser.facebookUserId)
                        val modelAndView = new ModelAndView(redirectView) with Errors
                        modelAndView.addError("Facebook account already in use")
                        result.set(modelAndView)
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
                        futureFacebookService.onSuccess {
                            case LocateByCodeResponse(_, Right(facebookService)) =>
                                eus.assignFacebookService(facebookService).onSuccess {
                                    case AssignFacebookServiceResponse(_, Left(error)) =>
                                        logger.debug("Error assigning Facebook service to EchoedUser {}: {}", echoedUser.id, error.message)
                                        val modelAndView = new ModelAndView(redirectView) with Errors;
                                        modelAndView.addError(error)
                                        modelAndView.addAllObjects(parameters)
                                        result.set(modelAndView)

                                    case AssignFacebookServiceResponse(_, Right(fs)) =>
                                        val modelAndView = new ModelAndView(redirectView);
                                        modelAndView.addAllObjects(parameters)
                                        result.set(modelAndView)
                                }
                        }
                })
            }
        }

        result
    }


    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam("code") code: String,
            @RequestParam(value = "redirect", required = false) redirect: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse): DeferredResult = {

        val result = new DeferredResult(new ModelAndView(facebookLoginErrorView))

        logger.debug("Requesting FacebookService with code {}", code)

        val index = httpServletRequest.getQueryString.indexOf("&code=")
        val queryString = "/facebook/login" +
            (if (index > -1) "?" + httpServletRequest.getQueryString.substring(0, index) else "")

        facebookServiceLocator.locateByCode(code, queryString).onSuccess {
            case LocateByCodeResponse(_, Right(facebookService)) => finishLogin(
                    httpServletResponse,
                    redirect,
                    facebookService,
                    httpServletRequest,
                    result)
        }

        result
    }

    private def finishLogin(
            httpServletResponse: HttpServletResponse,
            redirect: String,
            facebookService: FacebookService,
            httpServletRequest: HttpServletRequest,
            result: DeferredResult) = {

        echoedUserServiceLocator.getEchoedUserServiceWithFacebookService(facebookService).onSuccess {
            case LocateWithFacebookServiceResponse(_, Right(s))=> s.getEchoedUser.onSuccess {
                case GetEchoedUserResponse(_, Right(echoedUser))=>
                    cookieManager.addEchoedUserCookie(httpServletResponse, echoedUser, httpServletRequest)
                    val redirectView = "%s/%s" format(postLoginView, Option(redirect).getOrElse(""))
                    logger.debug("Redirecting to View: {} ", redirectView)
                    val modelAndView = new ModelAndView(redirectView)
                    result.set(modelAndView)
            }
        }
    }


    @RequestMapping(value = Array("/app"), method = Array(RequestMethod.POST))
    def app(
            @RequestParam(value = "signed_request", required = true) signedRequest: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(facebookLoginErrorView))

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
                facebookServiceLocator.locateByFacebookId(facebookId, accessToken).onSuccess {
                    case LocateByFacebookIdResponse(_, Right(facebookService)) => finishLogin(
                            httpServletResponse,
                            "?app=facebook",
                            facebookService,
                            httpServletRequest,
                            result)
                }
            },
            {
                val canvasPage = URLEncoder.encode(facebookCanvasApp, "UTF-8")
                result.set(new ModelAndView(
                    "authredirect",
                    "authUrl",
                    authUrl format(limitedPermissions, facebookClientId, canvasPage)))
            })

        result
    }


    def makeAuthorizeUrl(postAuthorizeUrl: String, add: Boolean = false, useExtendedPermissions: Boolean = true) = {
        val postUrl = "%s/facebook/%s?redirect=%s" format (
                siteUrl,
                if (add) "add" else "login",
                URLEncoder.encode(postAuthorizeUrl, "UTF-8"))

        authUrl format(
                if (useExtendedPermissions) extendedPermissions else limitedPermissions,
                facebookClientId,
                URLEncoder.encode(postUrl, "UTF-8"))
    }

}

