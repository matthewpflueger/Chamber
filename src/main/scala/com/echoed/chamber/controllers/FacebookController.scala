package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import com.echoed.chamber.services.echoeduser._
import org.springframework.web.bind.annotation.{RequestParam, RequestMapping, RequestMethod}
import java.util.{Map => JMap, HashMap => JHashMap}
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import java.net.URLEncoder
import org.apache.commons.codec.binary.Base64
import com.echoed.util.ScalaObjectMapper
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.springframework.web.context.request.async.DeferredResult
import scala.Right
import com.echoed.chamber.services.facebook.{FacebookAccessToken, FacebookCode}
import scala.concurrent.ExecutionContext.Implicits.global


@Controller
@RequestMapping(Array("/facebook"))
class FacebookController extends EchoedController with NetworkController {

    @BeanProperty var facebookClientId: String = _
    @BeanProperty var facebookClientSecret: String = _
    @BeanProperty var facebookCanvasApp: String = _

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
            eucc: EchoedUserClientCredentials,
            request: HttpServletRequest) = {

        val parameters = new JHashMap[String, Array[String]]()
        parameters.putAll(request.getParameterMap.asInstanceOf[JMap[String, Array[String]]])
        parameters.remove("code")

        val queryString = "/facebook/add" + {
            val index = request.getQueryString.indexOf("&code=")
            if (index > -1) "?" + request.getQueryString.substring(0, index)
            else ""
        }

        val redirectView = "%s/%s" format(v.postAddView, Option(redirect).getOrElse(""))

        log.debug("Redirect View {}" , redirectView)

        mp(AddFacebook(eucc, code, queryString))
        val modelAndView = new ModelAndView(redirectView)
        modelAndView.addAllObjects(parameters)
        modelAndView
    }


    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam("code") code: String,
            @RequestParam(value = "redirect", required = false) redirect: String,
            request: HttpServletRequest,
            response: HttpServletResponse) = {

        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.facebookLoginErrorView))

        log.debug("Requesting FacebookService with code {}", code)

        val index = request.getQueryString.indexOf("&code=")
        val queryString = "/facebook/login" +
            (if (index > -1) "?" + request.getQueryString.substring(0, index) else "")

        mp(LoginWithFacebook(Left(FacebookCode(code, queryString)))).onSuccess {
            case LoginWithFacebookResponse(_, Right(echoedUser)) =>
                cookieManager.addEchoedUserCookie(response, echoedUser, request)
                val redirectView = "%s?redirect=%s" format(v.postLoginView, Option(redirect).getOrElse(""))
                log.debug("Redirecting to View: {} ", redirectView)
                val modelAndView = new ModelAndView(redirectView)
                result.setResult(modelAndView)
        }

        result
    }


    @RequestMapping(value = Array("/app"), method = Array(RequestMethod.POST))
    def app(
            @RequestParam(value = "signed_request", required = true) signedRequest: String,
            request: HttpServletRequest,
            response: HttpServletResponse) = {

        val result = new DeferredResult[ModelAndView](null, new ModelAndView(v.facebookLoginErrorView))

        val parts = signedRequest.split("\\.")
        val encodedSig = parts(0)
        val encodedPayload = parts(1)

        val payload = new ScalaObjectMapper().readTree(new Base64(true).decode(encodedPayload))

        val algorithm = payload.get("algorithm").asText().replace("-", "")
        val mac = Mac.getInstance(algorithm)
        mac.init(new SecretKeySpec(facebookClientSecretBytes, algorithm))
        val encodedExpectedSig = Base64.encodeBase64URLSafeString(mac.doFinal(encodedPayload.getBytes))

        log.debug("sig {}", encodedSig)
        log.debug("sig {}", encodedExpectedSig)

        if (encodedExpectedSig != encodedSig) {
            throw new IllegalArgumentException("Invalid signature")
        }

        Option(payload.get("user_id")).map(_.asText()).cata(
            facebookId => {
                val accessToken = payload.get("oauth_token").asText()
                val redirect = "?app=facebook"
                mp(LoginWithFacebook(Right(FacebookAccessToken(accessToken, Option(facebookId))))).onSuccess {
                    case LoginWithFacebookResponse(_, Right(echoedUser)) =>
                        cookieManager.addEchoedUserCookie(response, echoedUser, request)
                        val redirectView = "%s/%s" format(v.postLoginView, Option(redirect).getOrElse(""))
                        log.debug("Redirecting to View: {} ", redirectView)
                        val modelAndView = new ModelAndView(redirectView)
                        result.setResult(modelAndView)
                }
            },
            {
                val canvasPage = URLEncoder.encode(facebookCanvasApp, "UTF-8")
                result.setResult(new ModelAndView(
                    "authredirect",
                    "authUrl",
                    authUrl format(limitedPermissions, facebookClientId, canvasPage)))
            })

        result
    }


    def makeAuthorizeUrl(postAuthorizeUrl: String, add: Boolean = false, useExtendedPermissions: Boolean = true) = {
        val postUrl = "%s/facebook/%s?redirect=%s" format (
                v.secureSiteUrl,
                if (add) "add" else "login",
                URLEncoder.encode(postAuthorizeUrl, "UTF-8"))

        authUrl format(
                if (useExtendedPermissions) extendedPermissions else limitedPermissions,
                facebookClientId,
                URLEncoder.encode(postUrl, "UTF-8"))
    }

}

