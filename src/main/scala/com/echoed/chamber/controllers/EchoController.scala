package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.continuation.{Continuation, ContinuationSupport}
import com.echoed.chamber.controllers.ControllerUtils._
import com.echoed.chamber.services.echoeduser._
import com.echoed.chamber.services.partner._
import com.echoed.chamber.domain.EchoClick
import com.echoed.chamber.services.EchoedException
import java.util.concurrent.atomic.AtomicLong
import scala.collection.JavaConversions._
import com.echoed.chamber.services.echo.{RecordEchoPossibilityResponse, EchoService, GetEchoByIdAndEchoedUserIdResponse}
import java.util.{Date, Map => JMap}
import java.text.DateFormat
import akka.dispatch.Promise
import akka.actor.ActorSystem
import org.springframework.web.context.request.async.DeferredResult

@Controller
@RequestMapping(Array("/echo"))
class EchoController {

    private final val logger = LoggerFactory.getLogger(classOf[EchoController])

    @BeanProperty var echoJsView: String = _
    @BeanProperty var echoJsNotActiveView: String = _
    @BeanProperty var echoJsErrorView: String = _
    @BeanProperty var echoLoginView: String = _
    @BeanProperty var echoLoginNotNeededView: String = _
    @BeanProperty var echoConfirmView: String = _
    @BeanProperty var echoFinishView: String = _
    @BeanProperty var echoDuplicateView: String = _
    @BeanProperty var echoAuthComplete: String = _
    @BeanProperty var echoIframe: String = _

    @BeanProperty var echoEchoedUrl: String = _
    @BeanProperty var echoRegisterUrl: String = _
    @BeanProperty var echoItView: String = _
    @BeanProperty var echoRedirectView: String = _
    @BeanProperty var echoCouponView: String = _

    @BeanProperty var buttonView: String = _
    @BeanProperty var loginView: String = _
    @BeanProperty var postLoginView: String = _
    @BeanProperty var confirmView: String = _
    @BeanProperty var errorView: String = _
    @BeanProperty var duplicateView: String = _
    @BeanProperty var facebookAddRedirectUrl: String = _
    @BeanProperty var facebookLoginRedirectUrl: String = _
    @BeanProperty var logoutUrl: String = _

    @BeanProperty var productGraphUrl: String = _

    @BeanProperty var echoService: EchoService = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var partnerServiceManager: PartnerServiceManager = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var networkControllers: JMap[String, NetworkController] = _

    @BeanProperty var actorSystem: ActorSystem = _

    val counter: AtomicLong = new AtomicLong(0);


    @RequestMapping(value = Array("/js"), method = Array(RequestMethod.GET), produces = Array("application/x-javascript"))
    def js(
            @RequestParam(value = "pid", required = true) pid: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(echoJsErrorView))

        partnerServiceManager.getView(pid).onSuccess {
            case GetViewResponse(_, Left(e: PartnerNotActive)) =>
                logger.debug("Partner Not Active: Serving Partner Not Active JS template")
                val modelAndView = new ModelAndView(echoJsNotActiveView)
                modelAndView.addObject("pid", pid)
                modelAndView.addObject("view", echoJsNotActiveView)
                result.set(modelAndView)
            case GetViewResponse(_, Right(vd)) =>
                val modelAndView = new ModelAndView(vd.view, vd.model)
                result.set(modelAndView)
        }

        result
    }


    @RequestMapping(value = Array("/request"), method = Array(RequestMethod.GET), produces = Array("application/json"))
    @ResponseBody
    def request(
            @RequestParam(value = "pid", required = true) pid: String,
            @RequestParam(value = "data", required = true) data: String,
            @RequestParam(value = "view", required = false) view: String,
            @RequestHeader(value = "Referer", required = false) referrerUrl: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            @RequestHeader(value = "User-Agent", required = true) userAgent: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        //browser id is required and should be set in the BrowserIdInterceptor...
        val bi = cookieManager.findBrowserIdCookie(httpServletRequest).get
        val eu= cookieManager.findEchoedUserCookie(httpServletRequest)
        val ec = cookieManager.findEchoClickCookie(httpServletRequest)
        val ip = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr)

        partnerServiceManager.requestEcho(
                partnerId = pid,
                request = data,
                browserId = bi,
                ipAddress = ip,
                userAgent = userAgent,
                referrerUrl = referrerUrl,
                echoedUserId = eu,
                echoClickId = ec,
                view = Option(view))
            .onSuccess {
                case RequestEchoResponse(_, Right(echoPossibilityView)) => result.set(echoPossibilityView)
            }

        result
    }



    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam(value = "id", required = true) id: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        val eu = cookieManager.findEchoedUserCookie(httpServletRequest)
        val ec = cookieManager.findEchoClickCookie(httpServletRequest)

        implicit val dispatcher = actorSystem.dispatcher


        val echoedUserNotFound = LocateWithIdResponse(LocateWithId(eu.orNull), Left(EchoedUserNotFound(eu.orNull)))
        val echoedUserResponse = eu.cata(
                echoedUserServiceLocator.getEchoedUserServiceWithId(_).recover { case e => echoedUserNotFound },
                Promise.successful(echoedUserNotFound))

        val recordEchoStepResponse  = partnerServiceManager.recordEchoStep(id, "login", eu, ec)

        for {
            eur <- echoedUserResponse
            resr <- recordEchoStepResponse
        } yield {
            resr match {
                case RecordEchoStepResponse(_, Left(EchoExists(_, _ , _))) =>
                    logger.debug("Product already echoed, Redirecting to echo/echoed")
                    val modelAndView = new ModelAndView(echoEchoedUrl, "id", id)
                    result.set(modelAndView)
                case RecordEchoStepResponse(_, Right(data)) =>
                    eur.cata(
                        e =>{
                            logger.debug("Requesting login for echo {}", data.echoPossibility.id)
                            val modelAndView = new ModelAndView(echoLoginView)

                            modelAndView.addObject("echoPossibilityView", data)
                            modelAndView.addObject("partnerLogo", data.partner.logo)
                            modelAndView.addObject("maxPercentage", "%1.0f".format(data.partnerSettings.maxPercentage*100));
                            //modelAndView.addObject("minPercentage", "%1.0f".format(data.partnerSettings.minPercentage*100));

                            if(data.partnerSettings.closetPercentage > 0)
                                modelAndView.addObject("closetPercentage", "%1.0f".format(data.partnerSettings.closetPercentage*100));

                            modelAndView.addObject("numberDays", data.partnerSettings.creditWindow / 24)
                            modelAndView.addObject("productPriceFormatted", "%.2f".format(data.echoPossibility.price));
                            modelAndView.addObject("minClicks", data.partnerSettings.minClicks)
                            result.set(modelAndView)
                        },
                        eus => {
                            logger.debug("Recognized user for echo login {}", eus.id)
                            val modelAndView = new ModelAndView(echoLoginNotNeededView, "id", id)
                            result.set(modelAndView)
                        })
            }
        }

        result
    }


    @RequestMapping(value = Array("/authorize"), method = Array(RequestMethod.GET))
    def authorize(
            @RequestParam(value = "id", required = true) id: String,
            @RequestParam(value = "network", required = true) network: String,
            @RequestParam(value = "add", required = false, defaultValue = "false") add: Boolean,
            @RequestParam(value = "close", required=false, defaultValue = "false") close: Boolean,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val networkController = networkControllers.get(network)
        val eu= cookieManager.findEchoedUserCookie(httpServletRequest)
        val ec = cookieManager.findEchoClickCookie(httpServletRequest)

        if (networkController == null) {
            val errorModelAndView = new ModelAndView(errorView) with Errors
            errorModelAndView.addError(EchoedException("Invalid network"))
            errorModelAndView
        } else {
            partnerServiceManager.recordEchoStep(id, "authorize-%s" format network, eu, ec)
            var authorizeUrl = ""
            if (close)
                authorizeUrl = networkController.makeAuthorizeUrl("echo/close?id=%s&network=%s" format (id, network), add)
            else
                authorizeUrl = networkController.makeAuthorizeUrl("echo/confirm?id=%s" format id, add)
            //val authorizeUrl = networkController.makeAuthorizeUrl("echo/confirm?id=%s" format id, add)
            logger.debug("Redirecting for authorization to {}", authorizeUrl)
            new ModelAndView("redirect:%s" format authorizeUrl)
        }
    }


    @RequestMapping(value = Array("iframe"), method = Array(RequestMethod.GET))
    def iframe(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = new ModelAndView(echoIframe)

    
    @RequestMapping(value = Array("/close"), method = Array(RequestMethod.GET))
    def close(
            @RequestParam(value = "id", required = true) id: String,
            @RequestParam(value = "network", required = true) network: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        val eu= cookieManager.findEchoedUserCookie(httpServletRequest)

        val ec = cookieManager.findEchoClickCookie(httpServletRequest)
        val echoedUserResponse = echoedUserServiceLocator
            .getEchoedUserServiceWithId(eu.get)
            .flatMap(_.resultOrException.getEchoedUser)

        val recordEchoStepResponse  = partnerServiceManager.locatePartnerByEchoId(id).flatMap(_ match {
            case LocateByEchoIdResponse(_, Left(e)) => throw e
            case LocateByEchoIdResponse(_, Right(ps)) => ps.recordEchoStep(id, "confirm", eu, ec)
        })

        for {
            eur <- echoedUserResponse
            resr <- recordEchoStepResponse
        } yield {

            val eu = eur.resultOrException

            resr.cata(
                _ match {
                    case EchoExists(epv, message, _) =>
                        val modelAndView = new ModelAndView(echoAuthComplete)
                        modelAndView.addObject("echoedUserName", eu.name)
                        modelAndView.addObject("facebookUserId", eu.facebookUserId)
                        modelAndView.addObject("twitterUserId", eu.twitterUserId)
                        modelAndView.addObject("network", network.capitalize)
                        result.set(modelAndView)
                    case e => result.set(new ModelAndView(echoAuthComplete))
                },
                epv => {
                    val modelAndView = new ModelAndView(echoAuthComplete)
                    modelAndView.addObject("echoedUserName", eu.name)
                    modelAndView.addObject("facebookUserId", eu.facebookUserId)
                    modelAndView.addObject("twitterUserId", eu.twitterUserId)
                    modelAndView.addObject("network", network.capitalize)
                    result.set(modelAndView)
                })
        }

        result
    }


    @RequestMapping(value = Array("/confirm"), method = Array(RequestMethod.GET))
    def confirm(
            @RequestParam(value = "id", required = true) id: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        val eu= cookieManager.findEchoedUserCookie(httpServletRequest)

        val ec = cookieManager.findEchoClickCookie(httpServletRequest)
        val echoedUserResponse = echoedUserServiceLocator
                .getEchoedUserServiceWithId(eu.get)
                .flatMap(_.resultOrException.getEchoedUser)

        val recordEchoStepResponse  = partnerServiceManager.locatePartnerByEchoId(id).flatMap(_ match {
            case LocateByEchoIdResponse(_, Left(e)) => throw e
            case LocateByEchoIdResponse(_, Right(ps)) => ps.recordEchoStep(id, "confirm", eu, ec)
        })

        for {
            eur <- echoedUserResponse
            resr <- recordEchoStepResponse
        } yield {
            val eu = eur.resultOrException
            resr.cata(
                _ match {
                    case EchoExists(epv, message, _) =>
                        logger.debug("Product already echoed {}, Redirecting to echo/echoed", epv.echo)
                        val modelAndView = new ModelAndView(echoEchoedUrl, "id", id)
                        result.set(modelAndView)
                },
                epv => {
                    val echoPossibility = epv.echoPossibility
                    val lu = "%s?redirect=%s" format(
                            logoutUrl,
                            URLEncoder.encode("echo/login?" + httpServletRequest.getQueryString.substring(0), "UTF-8"))
                    val modelAndView = new ModelAndView(echoConfirmView)
                    modelAndView.addObject("id", id)
                    modelAndView.addObject("echoedUser", eu)
                    modelAndView.addObject("echoPossibility", echoPossibility)
                    modelAndView.addObject("partner", epv.partner)
                    modelAndView.addObject("partnerSettings", epv.partnerSettings)
                    modelAndView.addObject("logoutUrl", lu)
                    modelAndView.addObject("partnerLogo", epv.partner.logo)
                    modelAndView.addObject("maxPercentage", "%1.0f".format(epv.partnerSettings.maxPercentage*100))
                    modelAndView.addObject("minPercentage", "%1.0f".format(epv.partnerSettings.minPercentage*100))
                    if(epv.partnerSettings.closetPercentage > 0)
                        modelAndView.addObject("closetPercentage", "%1.0f".format(epv.partnerSettings.closetPercentage*100))
                    modelAndView.addObject("minClicks", epv.partnerSettings.minClicks)
                    modelAndView.addObject("productPriceFormatted", "%.2f".format(epv.echoPossibility.price))
                    modelAndView.addObject("numberDays", epv.partnerSettings.creditWindow / 24)

                    result.set(modelAndView)
                })
        }

        result
    }

    @RequestMapping(value = Array("/finishjson"), method = Array(RequestMethod.GET))
    @ResponseBody
    def finishJSON(
            echoFinishParameters: EchoFinishParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoFinishParameters.echoedUserId = _)

        logger.debug("Echoing {}", echoFinishParameters)

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoFinishParameters.echoedUserId).onSuccess {
            case LocateWithIdResponse(_, Right(echoedUserService)) =>
                echoedUserService.echoTo(echoFinishParameters.createEchoTo).onSuccess {
                    case EchoToResponse(_, Left(DuplicateEcho(echo, message, _))) =>
                        result.set(echo)
                    case EchoToResponse(_, Right(echoFull)) =>
                        logger.debug("Received echo response {}", echoFull)
                        result.set(echoFull)
                        logger.debug("Successfully echoed {}", echoFull)
                }
        }

        result
    }


    @RequestMapping(value = Array("/finish"), method = Array(RequestMethod.GET))
    def finish(
            echoFinishParameters: EchoFinishParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoFinishParameters.echoedUserId = _)

        logger.debug("Echoing {}", echoFinishParameters)
        echoedUserServiceLocator.getEchoedUserServiceWithId(echoFinishParameters.echoedUserId).onSuccess {
            case LocateWithIdResponse(_, Right(echoedUserService)) =>
                echoedUserService.echoTo(echoFinishParameters.createEchoTo).onSuccess {
                    case EchoToResponse(_, Left(DuplicateEcho(echo, message, _))) =>
                        logger.debug("Received duplicate echo response to echo", echo)
                        result.set(new ModelAndView(echoEchoedUrl, "id", echoFinishParameters.getEchoId()))
                    case EchoToResponse(_, Right(echoFull)) =>
                        logger.debug("Received echo response {}", echoFull)
                        result.set(new ModelAndView(echoEchoedUrl, "id", echoFinishParameters.getEchoId()))
                        logger.debug("Successfully echoed {}", echoFull)
                }
        }

        result
    }


    @RequestMapping(value = Array("/echoed"), method=Array(RequestMethod.GET))
    def echoed(
            @RequestParam(value = "id", required = true) id: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        val eu = cookieManager.findEchoedUserCookie(httpServletRequest)

        val echoedUserResponse = echoedUserServiceLocator
            .getEchoedUserServiceWithId(eu.get)
            .flatMap(_.resultOrException.getEchoedUser)

        val partnerServiceResponse  = partnerServiceManager
            .locatePartnerByEchoId(id)
            .flatMap(_.resultOrException.getPartner)

        for {
            eur <- echoedUserResponse
            psr <- partnerServiceResponse
        } yield {
            val echoedUser = eur.resultOrException
            val partner = psr.resultOrException
            Option(echoedUser.email).getOrElse(None) match {
                case None => result.set(new ModelAndView(echoRegisterUrl + "?id=%s" format id))
                case email => echoService.getEchoByIdAndEchoedUserId(id, eu.get).onSuccess {
                    case GetEchoByIdAndEchoedUserIdResponse(_, Right(echo)) =>
                        val modelAndView = new ModelAndView(echoFinishView)
                        modelAndView.addObject("echo", echo)
                        modelAndView.addObject("partner", partner)
                        modelAndView.addObject("echoedUser", echoedUser)
                        result.set(modelAndView)
                }
            }
        }

        result
    }


    /*
        This is here because two old integrations (Trixie & Peanut and Redhawk Brigade) paint the share button by
        calling /echo/button
     */
    @RequestMapping(value = Array("/button"), method = Array(RequestMethod.GET))
    def button = "error"

    @RequestMapping(value = Array("/{linkId}"), method = Array(RequestMethod.GET))
    def click(
            @PathVariable(value = "linkId") linkId: String,
            @RequestHeader(value = "Referer", required = false) referrerUrl: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String,
            @RequestHeader(value = "User-Agent", required = false) userAgent: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        echoes(linkId, null, referrerUrl, remoteIp, userAgent, httpServletRequest, httpServletResponse)
    }

    @deprecated(message = "This will eventually go away as we do not need a postId anymore")
    @RequestMapping(value = Array("/{echoId}/{postId}"), method = Array(RequestMethod.GET))
    def echoes(
            @PathVariable(value = "echoId") echoId: String,
            @PathVariable(value = "postId") postId: String,
            @RequestHeader(value = "Referer", required = false) referrerUrl: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            @RequestHeader(value = "User-Agent", required = false) userAgent: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView(errorView))

        val echoClick = new EchoClick(
                echoId = null, //we will determine what the echoId is in the service...
                echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).orNull,
                browserId = cookieManager.findBrowserIdCookie(httpServletRequest).get,
                referrerUrl = referrerUrl,
                ipAddress = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr),
                userAgent = userAgent)
        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).orNull

        val partnerServiceManagerResponse = partnerServiceManager.getEcho(echoId)
        val echoServiceResponse = echoService.recordEchoClick(echoClick, echoId, postId)

        (for {
            psmr <- partnerServiceManagerResponse
            esr <- echoServiceResponse
        } yield {
            logger.debug("In yield of echo click")
            esr.resultOrException
            logger.debug("Successful echo service response")
            psmr match {
                case GetEchoResponse(msg, Right(epv)) =>
                    logger.debug("Got echo possibility view for echo {}", echoId)
                    cookieManager.findEchoClickCookie(httpServletRequest).getOrElse({
                        cookieManager.addEchoClickCookie(
                            httpServletResponse,
                            echoClick,
                            httpServletRequest)
                    })

                    val echo = epv.echoPossibilities.head
                    val partnerSettings = epv.partnerSettings
                    val partner = epv.partner
                    logger.debug("Returned Echo: {}", echo)

                    if (partnerSettings.couponCode != null && partnerSettings.couponExpiresOn.after(new Date())) {
                        logger.debug("Coupon Code!")
                        val modelAndView = new ModelAndView(echoCouponView)
                        modelAndView.addObject("echo", echo)
                        modelAndView.addObject("partnerSettings", partnerSettings)
                        modelAndView.addObject("partner", partner)
                        val df = DateFormat.getDateInstance(DateFormat.MEDIUM)
                        modelAndView.addObject("couponExpiresOn", df.format(partnerSettings.couponExpiresOn))
                        result.set(modelAndView)
                    } else {
                        logger.debug("No Coupon Code!")
                        val modelAndView = new ModelAndView(echoRedirectView)
                        modelAndView.addObject("echo", echo)
                        result.set(modelAndView)
                    }

                    echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onSuccess {
                        case LocateWithIdResponse(_, Right(eus)) =>
                            logger.debug("Publishing Action: ")
                            eus.publishFacebookAction("browse","product", productGraphUrl + echoId)
                    }
            }
        }).onSuccess {
            case _ => logger.debug("Successfully completed echo click {}", echoId)
        }.onFailure {
            case e => logger.error("Error during recording of echo click %s" format echoId, e)
        }

        result
    }
}


