package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.echoeduser.{EchoedUserServiceLocator,LocateWithIdResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.util.CookieManager
import akka.dispatch.Future
import com.echoed.chamber.domain.Echo
import org.springframework.web.bind.annotation._
import com.echoed.chamber.domain.{EchoClick, EchoPossibility}
import com.echoed.chamber.services.echo.{EchoResponseMessage, EchoRequestMessage, EchoService}
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping(Array("/echo"))
class EchoController {

    private final val logger = LoggerFactory.getLogger(classOf[EchoController])

    @BeanProperty var echoItView: String = _
    @BeanProperty var buttonView: String = _
    @BeanProperty var loginView: String = _
    @BeanProperty var confirmView: String = _
    @BeanProperty var errorView: String = _
    @BeanProperty var echoConfirm: String = _

    @BeanProperty var echoService: EchoService = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _

    @RequestMapping(value = Array("/button"), method = Array(RequestMethod.GET))
    def button(
            //TODO cookies should be encrypted
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletResponse: HttpServletResponse) = {
        if (echoedUserId != null) echoPossibilityParameters.echoedUserId = echoedUserId
        echoService.recordEchoPossibility(echoPossibilityParameters.createButtonEchoPossibility)
        new ModelAndView(buttonView)
    }


    @RequestMapping(method = Array(RequestMethod.GET))
    def echo(
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        if (echoedUserId != null) echoPossibilityParameters.echoedUserId = echoedUserId

        def loginModelAndView = {
            val echoPossibility = echoPossibilityParameters.createLoginEchoPossibility
            
            echoService.recordEchoPossibility(echoPossibility)
            val modelAndView = new ModelAndView(loginView)
//            modelAndView.addObject("redirectUrl", URLEncoder.encode("http://v1-api.echoed.com/facebook/login", "UTF-8"))
            modelAndView.addObject(
                "twitterUrl",
                //URLEncoder.encode(echoPossibility.asUrlParams("echo&"), "UTF-8"))
                URLEncoder.encode(echoPossibility.asUrlParams("echo?"), "UTF-8"))
                //"http://v1-api.echoed.com/twitter?redirect=echo&%s" format URLEncoder.encode(echoPossibility.asUrlParams(), "UTF-8"))
            

            modelAndView.addObject("redirectUrl",
                URLEncoder.encode("http://v1-api.echoed.com/facebook/login?redirect="
                    + URLEncoder.encode(echoPossibility.asUrlParams("echo?"),"UTF-8")))
            //modelAndView.addObject("echoPossibility",echoPossibility);

//            modelAndView.addObject("redirectUrl", URLEncoder.encode(
//                "http://v1-api.echoed.com/facebook/login?%s" format echoPossibility.generateUrlParameters,
//                "UTF-8"))
        }

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (Option(echoPossibilityParameters.echoedUserId) == None) {
            logger.debug("Unknown user trying to echo {}", echoPossibilityParameters)
            loginModelAndView
        } else if (continuation.isExpired) {
            logger.error("Request expired to echo {}", echoPossibilityParameters)
            loginModelAndView
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)
            val echoPoss = echoPossibilityParameters.createLoginEchoPossibility

            echoService.getEcho(echoPoss.id).map{
                tuple =>
                    if(tuple._2 == "Exists"){
                        logger.debug("Existing Echo {} ", tuple._1)
                        val modelAndView = new ModelAndView(errorView)
                        modelAndView.addObject("error_message", "This Item has already been shared")
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume
                    }
                    else{
                        echoedUserServiceLocator.getEchoedUserServiceWithId(echoPossibilityParameters.echoedUserId).onResult{
                            case LocateWithIdResponse(_,Left(error)) =>
                            case LocateWithIdResponse(_,Right(echoedUserService)) =>
                                echoedUserService.getEchoedUser.map { echoedUser =>
                                    val echoPossibility = echoPossibilityParameters.createConfirmEchoPossibility
                                    echoService.recordEchoPossibility(echoPossibility)
    
                                    val modelAndView = new ModelAndView(confirmView)
                                    modelAndView.addObject("echoedUser", echoedUser)
                                    modelAndView.addObject("echoPossibility", echoPossibility)
                                    modelAndView.addObject("facebookAddUrl",
                                        URLEncoder.encode(echoPossibility.asUrlParams("http://v1-api.echoed.com/facebook/login/add?redirect=echo?"), "UTF-8"))
                                    modelAndView.addObject("twitterAddUrl",
                                        URLEncoder.encode(echoPossibility.asUrlParams("echo?"), "UTF-8"))
                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume()
                            }
                        }
                    }

            }

            continuation.undispatch
        })
    }


    @RequestMapping(value = Array("/it"), method = Array(RequestMethod.GET))
    def it(
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            echoItParameters: EchoItParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        if (echoedUserId != null) echoItParameters.echoedUserId = echoedUserId

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        logger.debug("Echo It Parameters: {}", echoItParameters)

        if (continuation.isExpired) {
            logger.error("Request expired to echo ", echoItParameters)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)
            logger.debug("Echoing {}", echoItParameters)
            logger.debug("EchoPossibility Id {} , ", echoItParameters.echoPossibilityId)
            echoService.echo(echoItParameters.createEchoRequestMessage).map { echoResponseMessage =>
                    logger.debug("Received {}", echoResponseMessage)
                    val modelAndView = echoResponseMessage.fold(
                        errorMessage => new ModelAndView(errorView, "errorMessage", errorMessage),
                        echoFull => new ModelAndView(echoItView, "echoFull", echoFull)
                    )
                    continuation.setAttribute("modelAndView", modelAndView)
                    continuation.resume
            }
            continuation.undispatch()
        })

    }

    @RequestMapping(value = Array("/{echoId}/{postId}"), method = Array(RequestMethod.GET))
    def echoes(
            @PathVariable(value = "echoId") echoId: String,
            @PathVariable(value = "postId") postId: String,
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            @RequestHeader(value = "Referer", required = true) referrerUrl: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to record echo click for echo {}", echoId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)

            val echoClick = new EchoClick(echoId, echoedUserId, referrerUrl, httpServletRequest.getRemoteAddr)
            echoService.recordEchoClick(echoClick, postId).map { tuple =>
                cookieManager.addCookie(httpServletResponse, "echoClick", tuple._1.id)
                continuation.setAttribute("modelAndView", new ModelAndView("redirect:%s" format tuple._2))
                continuation.resume
            }
            continuation.undispatch()
        })

    }

}


