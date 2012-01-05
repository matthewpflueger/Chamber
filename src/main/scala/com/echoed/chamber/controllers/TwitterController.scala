package com.echoed.chamber.controllers


import com.echoed.chamber.services.twitter.TwitterServiceLocator
import com.echoed.chamber.services.twitter.TwitterService
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import org.springframework.stereotype.Controller
import reflect.BeanProperty
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory
import twitter4j.auth.{RequestToken, AccessToken}
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.dao.TwitterUserDao
import com.echoed.chamber.services.echo.EchoService
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.echoeduser.{EchoedUserService,LocateWithIdResponse,LocateWithTwitterServiceResponse,GetEchoedUserResponse,AssignTwitterServiceResponse}
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.util.CookieManager
import java.net.URLEncoder
import scala.collection.JavaConversions
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping(Array("/twitter"))
class TwitterController {

    private val logger = LoggerFactory.getLogger(classOf[TwitterController])


    @BeanProperty var twitterUserDao: TwitterUserDao = null
    @BeanProperty var twitterServiceLocator: TwitterServiceLocator = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoService: EchoService = _
    @BeanProperty var twitterRedirectUrl: String = null
    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var twitterLoginErrorView: String = _
    @BeanProperty var echoView: String = _

    @BeanProperty var facebookLoginErrorView: String = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def twitter(@CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
                //@CookieValue(value = "echoPossibility", required = false) echoPossibilityId: String,
                @RequestParam(value = "redirect", required = false) redirect: String,
                //echoPossibilityParameters: EchoPossibilityParameters,
                httpServletRequest: HttpServletRequest,
                httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(continuation.isExpired){
            new ModelAndView("test")
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)

            //val echoPossibility = echoPossibilityParameters.createTwitterEchoPossibility
            //val callbackUrl = echoPossibility.asUrlParams("http://v1-api.echoed.com/twitter/login?redirect=" + redirect +"&", true)
            var callbackUrl: String = null;

            val queryString = {
                //val index = httpServletRequest.getQueryString.indexOf("&code=")
                "?" + httpServletRequest.getQueryString.substring(0)
            }
            logger.debug("QueryString : {}", queryString)
            logger.debug("Twitter / Redirect : {}", redirect)
            logger.debug("EchoedUserId: {}", echoedUserId)
            if(echoedUserId != null){
                callbackUrl = "http://v1-api.echoed.com/twitter/add?redirect=" +  URLEncoder.encode(redirect,"UTF-8")
            }
            else{
                callbackUrl = "http://v1-api.echoed.com/twitter/login?redirect=" + URLEncoder.encode(redirect,"UTF-8")
            }

            logger.debug("Twitter Callback Url: {} ", URLEncoder.encode(callbackUrl,"UTF-8"));

            val futureTwitterService = twitterServiceLocator.getTwitterService(callbackUrl)
            futureTwitterService.onResult({
                case twitterService: TwitterService =>
                    logger.debug("Step 1 {}" , callbackUrl)

                    val futureRequestToken = twitterService.getRequestToken()
                    futureRequestToken.onResult({
                        case rt:RequestToken =>
                            continuation.setAttribute("modelAndView",{
                                val modelAndView: ModelAndView = new ModelAndView("redirect:" + rt.getAuthenticationURL)
                                modelAndView
                            })
                            continuation.resume()
                    })
            })
            continuation.undispatch()
        })
    }


    @RequestMapping(value = Array("/add"), method= Array(RequestMethod.GET))
    def add(
        @RequestParam("oauth_token") oAuthToken: String,
        @RequestParam("oauth_verifier") oAuthVerifier: String,
        @CookieValue(value = "echoedUserId", required = true) echoedUserId: String,
        @RequestParam(value = "redirect", required = false) redirect: String,
        httpServletRequest:HttpServletRequest,
        httpServletResponse:HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)


        if(continuation.isExpired){
            new ModelAndView("test")
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)
            //val futureEchoedUserService = echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId)

            logger.debug("Add/QueryString: {} ", redirect)
            val redirectView = "redirect:http://v1-api.echoed.com/" + redirect

            //futureEchoedUserService.onResult({
            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult{
                case LocateWithIdResponse(_,Left(error)) =>
                    logger.error("Error getting EchoedUserService: {}", error)
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    //val echoedUser = echoedUserService.echoedUser.get
                    echoedUserService.getEchoedUser.onResult{
                        case GetEchoedUserResponse(_,Left(error)) =>
                            logger.error("Error getting EchoedUser: {}", error)
                            //NEED TO ATTACH ERROR MODEL
                        case GetEchoedUserResponse(_,Right(echoedUser)) =>
                            if(echoedUser.twitterUserId != null){
                                //Twitter Account Already Exists
                            }
                            else{
                                val futureTwitterService = twitterServiceLocator.getTwitterServiceWithToken(oAuthToken)
                                futureTwitterService.onResult({
                                    case twitterService: TwitterService=>
                                        val accessToken = twitterService.getAccessToken(oAuthVerifier)
                                        accessToken.onResult({
                                            case aToken: AccessToken =>
                                                val twitterServiceWithAccessToken = twitterServiceLocator.getTwitterServiceWithAccessToken(aToken)
                                                twitterServiceWithAccessToken.onResult({
                                                    case ts: TwitterService=>
                                                        echoedUserService.assignTwitterService(ts).onResult({
                                                            case AssignTwitterServiceResponse(_, Left(error)) =>
                                                                logger.info("Error Assigning TwitterService: {}", error.getMessage)
                                                                val modelAndView = new ModelAndView(redirectView)
                                                                modelAndView.addObject("error", error.getMessage)
                                                                continuation.setAttribute("modelAndView", modelAndView)
                                                                continuation.resume
                                                            case AssignTwitterServiceResponse(_, Right(ts2)) =>
                                                                val modelAndView = new ModelAndView(redirectView)
                                                                continuation.setAttribute("modelAndView", modelAndView)
                                                                continuation.resume
                                                        })
                                                        .onException({
                                                            case e =>
                                                                logger.error("Exception thrown Assinging TwitterService: {}", e)
                                                        })

                                                })
                                        })
                                })
                            }
                    }
                    .onException{
                        case e=>
                            logger.error("Exception thrown Getting EchoedUser: {}" , e)
                    }
            }
            .onException{
                case e=>
                    logger.error("Exception thrown Getting EchoedUserService: {}" , e)
            }
            continuation.undispatch()
        })
    }



    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(@RequestParam("oauth_token") oAuthToken: String,
              @RequestParam("oauth_verifier") oAuthVerifier: String,
              @RequestParam(value = "redirect", required = false) redirect: String,
              echoPossibilityParameters: EchoPossibilityParameters,
              httpServletRequest: HttpServletRequest,
              httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)


        if (Option(oAuthToken) == None || oAuthVerifier == None || continuation.isExpired) {
            logger.error("Request expired to login via Twitter with code {}")
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)

            val queryString = httpServletRequest.getQueryString

            logger.debug("Twitter/Login/QueryString : {}", queryString)
            logger.debug("Twitter/Login/Redirect: {}", redirect)

            val futureTwitterService = twitterServiceLocator.getTwitterServiceWithToken(oAuthToken)
            futureTwitterService
                    .onResult({
                case twitterService: TwitterService =>
                    logger.debug("Received twitterservice with oAuthToken {}", oAuthToken)
                    val accessToken = twitterService.getAccessToken(oAuthVerifier)
                    accessToken.onResult({
                        case aToken: AccessToken =>
                            logger.debug("Received AccessToken with oAuthVerifier {}", oAuthVerifier)
                            val twitterServiceWithAccessToken = twitterServiceLocator.getTwitterServiceWithAccessToken(aToken)
                            twitterServiceWithAccessToken.onResult({
                                case ts: TwitterService =>
                                    logger.debug("Requesting EchoedUserService with TwitterService {}", ts)
                                    val futureEchoedUserService = echoedUserServiceLocator.getEchoedUserServiceWithTwitterService(ts)
                                    logger.debug("Received EchoedUserService {} with TwitterService {}", futureEchoedUserService, ts)
                                    echoedUserServiceLocator.getEchoedUserServiceWithTwitterService(ts).onResult({
                                        case LocateWithTwitterServiceResponse(_,Left(error)) =>
                                            logger.error("Error Locating EchoedUserServiceWithTwitterService: {} ", error)
                                        case LocateWithTwitterServiceResponse(_,Right(es)) =>
                                            logger.debug("Successfully recieved EchoedUserService {} with TwitterService {}", es, ts)
                                            es.getEchoedUser.onResult{
                                                case GetEchoedUserResponse(_,Left(error)) =>
                                                    logger.error("Error getting EchoedUser: {}", error)
                                                    val modelAndView = new ModelAndView(echoView)
                                                    continuation.setAttribute("modelAndView", modelAndView)
                                                    continuation.resume()
                                                case GetEchoedUserResponse(_,Right(echoedUser))=>
                                                    ts.assignEchoedUserId(echoedUser.id)
                                                    logger.debug("Added Cookie EchoedUserId: {}", echoedUser)
                                                    cookieManager.addCookie(httpServletResponse, "echoedUserId", echoedUser.id)
                                                    val redirectView = "redirect:http://v1-api.echoed.com/" + redirect
                                                    logger.debug("Redirecting to View: {} ", redirectView)
                                                    val modelAndView = new ModelAndView(redirectView)
                                                    continuation.setAttribute("modelAndView", modelAndView)
                                                    continuation.resume()
                                            }
                                            .onException{
                                                case e =>
                                                    logger.error("Exception getting EchoedUser: {}", e)
                                                    val modelAndView = new ModelAndView(echoView)
                                                    continuation.setAttribute("modelAndView", modelAndView)
                                                    continuation.resume()
                                            }
                                    })
                            })
                            .onException({
                                case e =>
                                    continuation.setAttribute("modelAndView", new ModelAndView(twitterLoginErrorView))
                                    continuation.resume()
                            })
                    })
            })
            .onException({
                case e =>
                    continuation.setAttribute("modelAndView", new ModelAndView(twitterLoginErrorView))
                    continuation.resume()
            })
            continuation.undispatch()
        })
    }
}
