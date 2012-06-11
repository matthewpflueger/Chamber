package com.echoed.chamber.controllers


import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import org.springframework.stereotype.Controller
import reflect.BeanProperty
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.echo.EchoService
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.echoeduser.{LocateWithIdResponse,LocateWithTwitterServiceResponse,GetEchoedUserResponse,AssignTwitterServiceResponse}
import org.eclipse.jetty.continuation.ContinuationSupport
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.twitter._
import org.springframework.web.context.request.async.DeferredResult


@Controller
@RequestMapping(Array("/twitter"))
class TwitterController extends NetworkController {

    private val logger = LoggerFactory.getLogger(classOf[TwitterController])


    @BeanProperty var twitterServiceLocator: TwitterServiceLocator = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoService: EchoService = _
    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var errorView: String = _
    @BeanProperty var postAddView: String = _
    @BeanProperty var postLoginView: String = _
    @BeanProperty var siteUrl: String = _


    @RequestMapping(method = Array(RequestMethod.GET))
    def twitter(@RequestParam(value = "redirect", required = false) redirect: String,
                httpServletRequest: HttpServletRequest,
                httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        val queryString = "?" + httpServletRequest.getQueryString.substring(0)

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest)

        logger.debug("QueryString : {}", queryString)
        logger.debug("Twitter / Redirect : {}", redirect)
        logger.debug("EchoedUserId: {}", echoedUserId)

        val callbackUrl = "%s/twitter/%s?redirect=%s" format(
                siteUrl,
                echoedUserId.map(_ => "add").getOrElse("login"),
                URLEncoder.encode(redirect, "UTF-8"))

        logger.debug("Twitter Callback Url: {} ", URLEncoder.encode(callbackUrl,"UTF-8"));

        twitterServiceLocator.getTwitterService(callbackUrl).onSuccess {
            case GetTwitterServiceResponse(_, Right(twitterService)) => twitterService.getRequestToken.onSuccess {
                case GetRequestTokenResponse(_, Right(requestToken)) =>
                    result.set(new ModelAndView("redirect:" + requestToken.getAuthenticationURL))
            }
        }

        result
    }


    @RequestMapping(value = Array("/add"), method= Array(RequestMethod.GET))
    def add(@RequestParam("oauth_token") oAuthToken: String,
            @RequestParam("oauth_verifier") oAuthVerifier: String,
            @RequestParam(value = "redirect", required = false) redirect: String,
            httpServletRequest:HttpServletRequest,
            httpServletResponse:HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        logger.debug("Add/QueryString: {} ", redirect)
        val redirectView = postAddView + redirect //"redirect:http://www.echoed.com/" + redirect

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest)

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId.get).onSuccess {
            case LocateWithIdResponse(_, Right(echoedUserService)) => echoedUserService.getEchoedUser.onSuccess {
                case GetEchoedUserResponse(_, Right(echoedUser)) => twitterServiceLocator.getTwitterServiceWithToken(oAuthToken).onSuccess {
                    case GetTwitterServiceWithTokenResponse(_, Right(twitterService)) => twitterService.getAccessToken(oAuthVerifier).onSuccess {
                        case GetAccessTokenResponse(_, Right(aToken)) => twitterServiceLocator.getTwitterServiceWithAccessToken(aToken).onSuccess {
                            case GetTwitterServiceWithAccessTokenResponse(_, Right(ts)) => echoedUserService.assignTwitterService(ts).onSuccess {
                                case AssignTwitterServiceResponse(_, Right(_)) =>
                                    result.set(new ModelAndView(redirectView))
                            }
                        }
                    }
                }
            }
        }

        result
    }



    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(@RequestParam("oauth_token") oAuthToken: String,
              @RequestParam("oauth_verifier") oAuthVerifier: String,
              @RequestParam(value = "redirect", required = false) redirect: String,
              echoPossibilityParameters: EchoPossibilityParameters,
              httpServletRequest: HttpServletRequest,
              httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        val queryString = httpServletRequest.getQueryString

        logger.debug("Twitter/Login/QueryString: {}", queryString)
        logger.debug("Twitter/Login/Redirect: {}", redirect)

        twitterServiceLocator.getTwitterServiceWithToken(oAuthToken).onSuccess {
            case GetTwitterServiceWithTokenResponse(_, Right(twitterService)) => twitterService.getAccessToken(oAuthVerifier).onSuccess {
                case GetAccessTokenResponse(_, Right(aToken)) => twitterServiceLocator.getTwitterServiceWithAccessToken(aToken).onSuccess {
                    case GetTwitterServiceWithAccessTokenResponse(_, Right(ts)) => echoedUserServiceLocator.getEchoedUserServiceWithTwitterService(ts).onSuccess {
                        case LocateWithTwitterServiceResponse(_, Right(es)) => es.getEchoedUser.onSuccess {
                            case GetEchoedUserResponse(_, Right(echoedUser)) =>
                                ts.assignEchoedUser(echoedUser.id)
                                cookieManager.addEchoedUserCookie(
                                        httpServletResponse,
                                        echoedUser,
                                        httpServletRequest)
                                val redirectView = postLoginView + redirect //"redirect:http://www.echoed.com/" + redirect
                                logger.debug("Redirecting to View: {} ", redirectView)
                                result.set(new ModelAndView(redirectView))
                        }
                    }
                }
            }
        }

        result
    }

    def makeAuthorizeUrl(postAuthorizeUrl: String, add: Boolean = false, useExtendedPermissions: Boolean = true) =
        "%s/twitter?redirect=%s" format(siteUrl, URLEncoder.encode(postAuthorizeUrl, "UTF-8"))

}
