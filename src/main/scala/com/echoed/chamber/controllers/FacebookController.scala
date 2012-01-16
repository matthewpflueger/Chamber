package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.facebook.{FacebookService, FacebookServiceLocator}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator,LocateWithFacebookServiceResponse,LocateWithIdResponse,GetEchoedUserResponse,AssignFacebookServiceResponse}
import com.echoed.util.CookieManager
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import com.echoed.chamber.services.echo.EchoService
import java.util.{HashMap => JMap}
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import akka.actor.Actor


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
            @RequestParam(value="redirect",required = false)  redirect: String,
            @CookieValue(value="echoedUserId", required= true) echoedUserId: String,
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {



        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(Option(code) == None || continuation.isExpired){
            logger.error("Request expired to login via Facebook with code{}" , code)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {

            continuation.suspend(httpServletResponse)

            def error(e: Throwable) {
                logger.error("Unexpected error adding Facebook account", e)
                //FIXME need to add error model and view...
            }

            val parameters = new JMap[String, Array[String]]()
            parameters.putAll(httpServletRequest.getParameterMap)
            parameters.remove("code")

            val redirectView = "redirect:http://v1-api.echoed.com/" + redirect

            logger.debug("Redirect View {}" , redirectView)


            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onComplete(_.value.get.fold(
                e => error(e),
                locateWithIdResponse => locateWithIdResponse match {
                    case LocateWithIdResponse(_, Left(e)) => error(e)
                    case LocateWithIdResponse(_, Right(eus)) => eus.getEchoedUser.onComplete(_.value.get.fold(
                        e => error(e),
                        getEchoedUserResponse => getEchoedUserResponse match {
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

                                    val futureFacebookService = facebookServiceLocator.getFacebookServiceWithCode(code, queryString)
                                    futureFacebookService.onComplete(_.value.get.fold(
                                        e => error(e),
                                        facebookService => eus.assignFacebookService(facebookService).onComplete(_.value.get.fold(
                                            e => error(e),
                                            assignFacebookServiceResponse => assignFacebookServiceResponse match {
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
                                            }
                                        ))
                                    ))
                                })
                        }
                    ))
                }
            ))

            continuation.undispatch()
        }
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
                "/facebook/login?" + httpServletRequest.getQueryString.substring(0, index)
            }
            val futureFacebookService = facebookServiceLocator.getFacebookServiceWithCode(code, queryString)

            futureFacebookService
                    .onResult {
                        case facebookService: FacebookService =>
                            logger.debug("Received FacebookService with code {}", code)
                            logger.debug("Requesting EchoedUserService with FacebookService with code {}", code)
                            echoedUserServiceLocator.getEchoedUserServiceWithFacebookService(facebookService)
                                    .onResult {
                                        case LocateWithFacebookServiceResponse(_,Left(e)) =>
                                            logger.error("Unexpected result {}", e)
                                            continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                            continuation.resume
                                        case LocateWithFacebookServiceResponse(_,Right(s))=>
                                            logger.debug("Received EchoedUserService using FacebookService with code {}", code)
                                            var redirectView: String = null
                                            s.getEchoedUser.onResult{
                                                case GetEchoedUserResponse(_,Left(error)) =>
                                                    logger.error("Error retrieving EchoedUser: {}", error)
                                                    continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                                    continuation.resume()
                                                case GetEchoedUserResponse(_,Right(echoedUser))=>
                                                    cookieManager.addCookie(httpServletResponse, "echoedUserId", echoedUser.id)
                                                    redirectView = "redirect:http://v1-api.echoed.com/" + redirect;
                                                    logger.debug("Redirecting to View: {} ", redirectView);
                                                    val modelAndView = new ModelAndView(redirectView);
                                                    continuation.setAttribute("modelAndView", modelAndView)
                                                    continuation.resume()
                                            }
                                            .onException{
                                                case e =>
                                                    logger.error("Exception thrown getting EchoedUser: {]", e)
                                                    continuation.setAttribute("modelAndView", new ModelAndView(facebookLoginErrorView))
                                                    continuation.resume
                                            }
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

