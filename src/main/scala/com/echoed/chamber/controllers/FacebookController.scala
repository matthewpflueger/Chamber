package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.facebook.{FacebookService, FacebookServiceLocator}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator,LocateWithFacebookServiceResponse,LocateWithIdResponse}
import com.echoed.util.CookieManager
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import com.echoed.chamber.services.echo.EchoService
import scala.collection.JavaConversions
import org.springframework.web.servlet.ModelAndView


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
    
    @RequestMapping(value = Array("/login/add"), method = Array(RequestMethod.GET))
    def add(
            @RequestParam("code") code:String,
            @RequestParam(value="redirect",required = false)  redirect: String,
            @CookieValue(value="echoedUserId", required= true) echoedUserId: String,
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {



        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(Option(code) == None || continuation.isExpired){
            logger.error("Request expired to login via Facebook with code{}" , code)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)
            
            val queryString = {
                val index = httpServletRequest.getQueryString.indexOf("&code=")
                "/add?" + httpServletRequest.getQueryString.substring(0,index)

            }

            val redirectView = "redirect:http://v1-api.echoed.com/" + redirect + "?" + queryString

            logger.debug("Redirect View {}" , redirectView)


            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId)
                    .onResult {
                        case LocateWithIdResponse(_,Left(error)) =>
                        case LocateWithIdResponse(_,Right(echoedUserService)) =>
                            val echoedUser = echoedUserService.echoedUser.get
                            if(echoedUser.facebookUserId == null){
                                //Check to make sure there is no facebook user attached
                                logger.debug("No Existing Facebook User Id ")
                                val futureFacebookService = facebookServiceLocator.getFacebookServiceWithCode(code,queryString)
                                
                                futureFacebookService
                                    .onResult{
                                        case facebookService:FacebookService =>
                                            logger.debug("Attaching Facebook Service {} to EchoedUserService {} ", facebookService,echoedUserService)
                                            val futureFs = echoedUserService.assignFacebookService(facebookService)
                                            futureFs.onResult({
                                                case fs:FacebookService =>
                                                    val modelAndView = new ModelAndView(redirectView);
                                                    continuation.setAttribute("modelAndView", modelAndView)
                                                    continuation.resume
                                            })

                                }
                            }
                            else{
                                logger.debug("Facebook User Id already attached {}", echoedUser.facebookUserId)
                                val modelAndView = new ModelAndView(redirectView);
                                continuation.setAttribute("modelAndView",modelAndView)
                                continuation.resume
                            }
            }
            continuation.undispatch()
        })
    }


    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam("code") code: String,
            @RequestParam(value = "redirect", required = false) redirect: String,
            //echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        logger.debug("Redirect Parameter: {}" ,redirect);

        //val echoPossibility = echoPossibilityParameters.createFacebookEchoPossibility

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (Option(code) == None || continuation.isExpired) {
            logger.error("Request expired to login via Facebook with code {}", code)
            //echoService.recordEchoPossibility(echoPossibility)
            new ModelAndView(facebookLoginErrorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)

            logger.debug("Requesting FacebookService with code {}", code)
            val queryString = {
                val index = httpServletRequest.getQueryString.indexOf("&code=")
                "?" + httpServletRequest.getQueryString.substring(0, index)
            }
            val futureFacebookService = facebookServiceLocator.getFacebookServiceWithCode(code, queryString)

            futureFacebookService
                    .onResult {
                        case facebookService: FacebookService =>
                            logger.debug("Received FacebookService with code {}", code)
                            logger.debug("Requesting EchoedUserService with FacebookService with code {}", code)
                            //val futureEchoedUserService = echoedUserServiceLocator.getEchoedUserServiceWithFacebookService(facebookService)
                            //futureEchoedUserService
                            echoedUserServiceLocator.getEchoedUserServiceWithFacebookService(facebookService)
                                    .onResult {
                                        //case s: EchoedUserService =>
                                        case LocateWithFacebookServiceResponse(_,Right(s))=>
                                            logger.debug("Received EchoedUserService using FacebookService with code {}", code)
                                            continuation.setAttribute("modelAndView",
                                                try {
                                                    val echoedUser = s.echoedUser.get
                                                    cookieManager.addCookie(httpServletResponse, "echoedUserId", echoedUser.id)
                                                    val redirectView = "redirect:http://v1-api.echoed.com/" + redirect;
                                                    logger.debug("Redirecting to View: {} ", redirectView);
                                                    val modelAndView = new ModelAndView(redirectView);
                                                    modelAndView

                                                    //modelAndView.addAllObjects(JavaConversions.mapAsJavaMap[String, String](
                                                    //        echoPossibility.asMap))
                                                    //modelAndView.addObject("echoedUserId", echoedUser.id)
                                                } catch {
                                                    case n: NoSuchElementException =>
                                                        logger.error("Error getting EchoedUser {}", n)
                                                        new ModelAndView(facebookLoginErrorView)
                                                    case e =>
                                                        //logger.debug("No EchoPossibility with id {}", echoPossibilityId)
                                                        new ModelAndView(dashboardView)
                                                })
                                            continuation.resume
                                        case LocateWithFacebookServiceResponse(_,Left(e)) =>
                                            logger.error("Unexpected result {}", e)
                                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                            continuation.resume
                                    }
                                    .onException {
                                        case e =>
                                            logger.error("Failed to receive EchoedUserService with code {} due to {}", code, e)
                                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                            continuation.resume
                                    }
                                    .onTimeout(
                                        _ => {
                                            logger.error("Timeout requesting EchoedUserService with code {}", code)
                                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                            continuation.resume
                                        }
                                    )
                        case e =>
                            logger.error("Unexpected result {}", e)
                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                            continuation.resume
                    }
                    .onException {
                        case e =>
                            logger.error("Failed to receive FacebookService with code {} due to {}", code, e)
                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                            continuation.resume
                    }
                    .onTimeout(
                        _ => {
                            logger.error("Timeout requesting FacebookService with code {}", code)
                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                            continuation.resume
                        }
                    )

            continuation.undispatch
        })

    }

}

