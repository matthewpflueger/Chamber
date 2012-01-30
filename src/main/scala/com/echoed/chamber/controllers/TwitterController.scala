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
import com.echoed.util.CookieManager
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.twitter._


@Controller
@RequestMapping(Array("/twitter"))
class TwitterController {

    private val logger = LoggerFactory.getLogger(classOf[TwitterController])


    @BeanProperty var twitterServiceLocator: TwitterServiceLocator = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoService: EchoService = _
    @BeanProperty var twitterRedirectUrl: String = null
    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var twitterLoginErrorView: String = _
    @BeanProperty var echoView: String = _
    @BeanProperty var errorView: String = _


    @RequestMapping(method = Array(RequestMethod.GET))
    def twitter(@CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
                @RequestParam(value = "redirect", required = false) redirect: String,
                httpServletRequest: HttpServletRequest,
                httpServletResponse: HttpServletResponse) = {
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) = {
            logger.error("Unexpected error", e)
            val modelAndView = new ModelAndView(errorView, "errorMessage", e.getMessage)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume
            modelAndView
        }

        if (continuation.isExpired) {
            error(RequestExpiredException("We encountered an while talking to Twitter"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            val queryString = "?" + httpServletRequest.getQueryString.substring(0)

            logger.debug("QueryString : {}", queryString)
            logger.debug("Twitter / Redirect : {}", redirect)
            logger.debug("EchoedUserId: {}", echoedUserId)

            val callbackUrl = if (echoedUserId != null) {
                "http://www.echoed.com/twitter/add?redirect=" +  URLEncoder.encode(redirect,"UTF-8")
            } else {
                "http://www.echoed.com/twitter/login?redirect=" + URLEncoder.encode(redirect,"UTF-8")
            }

            logger.debug("Twitter Callback Url: {} ", URLEncoder.encode(callbackUrl,"UTF-8"));

            twitterServiceLocator.getTwitterService(callbackUrl).onResult {
                case GetTwitterServiceResponse(_, Left(e)) => error(e)
                case GetTwitterServiceResponse(_, Right(twitterService)) => twitterService.getRequestToken.onResult {
                    case GetRequestTokenResponse(_, Left(e)) => error(e)
                    case GetRequestTokenResponse(_, Right(requestToken)) =>
                        val modelAndView = new ModelAndView("redirect:" + requestToken.getAuthenticationURL)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume
                    }.onException { case e => error(e) }
            }.onException { case e => error(e) }

            continuation.undispatch
        }
    }


    @RequestMapping(value = Array("/add"), method= Array(RequestMethod.GET))
    def add(@RequestParam("oauth_token") oAuthToken: String,
            @RequestParam("oauth_verifier") oAuthVerifier: String,
            @CookieValue(value = "echoedUserId", required = true) echoedUserId: String,
            @RequestParam(value = "redirect", required = false) redirect: String,
            httpServletRequest:HttpServletRequest,
            httpServletResponse:HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) = {
            logger.error("Unexpected error", e)
            val modelAndView = new ModelAndView(errorView, "errorMessage", e.getMessage)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume
            modelAndView
        }

        if (continuation.isExpired) {
            error(RequestExpiredException("We encountered an error adding your Twitter account to Echoed"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)


            logger.debug("Add/QueryString: {} ", redirect)
            val redirectView = "redirect:http://www.echoed.com/" + redirect

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_, Left(e)) => error(e)
                case LocateWithIdResponse(_, Right(echoedUserService)) => echoedUserService.getEchoedUser.onResult {
                    case GetEchoedUserResponse(_, Left(e)) => error(e)
                    case GetEchoedUserResponse(_, Right(echoedUser)) => twitterServiceLocator.getTwitterServiceWithToken(oAuthToken).onResult {
                        case GetTwitterServiceWithTokenResponse(_, Left(e)) => error(e)
                        case GetTwitterServiceWithTokenResponse(_, Right(twitterService)) => twitterService.getAccessToken(oAuthVerifier).onResult {
                            case GetAccessTokenResponse(_, Left(e)) => error(e)
                            case GetAccessTokenResponse(_, Right(aToken)) => twitterServiceLocator.getTwitterServiceWithAccessToken(aToken).onResult {
                                case GetTwitterServiceWithAccessTokenResponse(_, Left(e)) => error(e)
                                case GetTwitterServiceWithAccessTokenResponse(_, Right(ts)) => echoedUserService.assignTwitterService(ts).onResult {
                                    case AssignTwitterServiceResponse(_, Left(e)) => error(e)
                                    case AssignTwitterServiceResponse(_, Right(_)) =>
                                        val modelAndView = new ModelAndView(redirectView)
                                        continuation.setAttribute("modelAndView", modelAndView)
                                        continuation.resume
                                }.onException { case e => error(e) }
                            }.onException { case e => error(e) }
                        }.onException { case e => error(e) }
                    }.onException { case e => error(e) }
                }.onException { case e => error(e) }
            }.onException { case e => error(e) }

            continuation.undispatch()
        }
    }



    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(@RequestParam("oauth_token") oAuthToken: String,
              @RequestParam("oauth_verifier") oAuthVerifier: String,
              @RequestParam(value = "redirect", required = false) redirect: String,
              echoPossibilityParameters: EchoPossibilityParameters,
              httpServletRequest: HttpServletRequest,
              httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        def error(e: Throwable) = {
            logger.error("Unexpected error", e)
            val modelAndView = new ModelAndView(errorView, "errorMessage", e.getMessage)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume
            modelAndView
        }

        if (Option(oAuthToken) == None || oAuthVerifier == None || continuation.isExpired) {
            error(RequestExpiredException("We encountered an error logging you in via Twitter"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            val queryString = httpServletRequest.getQueryString

            logger.debug("Twitter/Login/QueryString: {}", queryString)
            logger.debug("Twitter/Login/Redirect: {}", redirect)

            twitterServiceLocator.getTwitterServiceWithToken(oAuthToken).onResult {
                case GetTwitterServiceWithTokenResponse(_, Left(e)) => error(e)
                case GetTwitterServiceWithTokenResponse(_, Right(twitterService)) => twitterService.getAccessToken(oAuthVerifier).onResult {
                    case GetAccessTokenResponse(_, Left(e)) => error(e)
                    case GetAccessTokenResponse(_, Right(aToken)) => twitterServiceLocator.getTwitterServiceWithAccessToken(aToken).onResult {
                        case GetTwitterServiceWithAccessTokenResponse(_, Left(e)) => error(e)
                        case GetTwitterServiceWithAccessTokenResponse(_, Right(ts)) => echoedUserServiceLocator.getEchoedUserServiceWithTwitterService(ts).onResult {
                            case LocateWithTwitterServiceResponse(_, Left(e)) => error(e)
                            case LocateWithTwitterServiceResponse(_, Right(es)) => es.getEchoedUser.onResult {
                                case GetEchoedUserResponse(_, Left(e)) => error(e)
                                case GetEchoedUserResponse(_, Right(echoedUser)) =>
                                    ts.assignEchoedUser(echoedUser.id)
                                    cookieManager.addCookie(httpServletResponse, "echoedUserId", echoedUser.id)
                                    val redirectView = "redirect:http://www.echoed.com/" + redirect
                                    logger.debug("Redirecting to View: {} ", redirectView)
                                    continuation.setAttribute("modelAndView", new ModelAndView(redirectView))
                                    continuation.resume
                            }.onException { case e => error(e) }
                        }.onException { case e => error(e) }
                    }.onException { case e => error(e) }
                }.onException { case e => error(e) }
            }.onException { case e => error(e) }

            continuation.undispatch()
        }
    }
}
