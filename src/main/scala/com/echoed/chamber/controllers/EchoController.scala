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
import akka.dispatch.{AlreadyCompletedFuture, Future}
import com.echoed.chamber.services.echoeduser._
import com.echoed.chamber.services.partner._
import com.echoed.chamber.domain.{EchoedUser, EchoClick}
import java.util.{Map => JMap}
import com.echoed.chamber.services.EchoedException
import java.util.concurrent.atomic.AtomicLong
import scala.collection.JavaConversions._
import com.echoed.chamber.services.echo.{RecordEchoClickResponse, EchoExists, RecordEchoPossibilityResponse, EchoService, GetEchoByIdResponse}

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

    @BeanProperty var echoRegisterUrl: String = _
    @BeanProperty var echoItView: String = _
    @BeanProperty var echoRedirectView: String = _

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

    val counter: AtomicLong = new AtomicLong(0);


    @RequestMapping(value = Array("/js"), method = Array(RequestMethod.GET), produces = Array("application/x-javascript"))
    def js(
            @RequestParam(value = "pid", required = true) pid: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            RequestExpiredException()
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            partnerServiceManager.getView(pid).onComplete(_.value.get.fold(
                e => error(echoJsErrorView, Some(e)),
                _ match {
                    case GetViewResponse(_, Left(e: PartnerNotActive)) =>
                        logger.debug("Partner Not Active: Serving Partner Not Active JS template")
                        val modelAndView = new ModelAndView(echoJsNotActiveView)
                        modelAndView.addObject("pid", pid)
                        continuation.setAttribute("modelAndView",modelAndView)
                        continuation.resume()
                    case GetViewResponse(_, Left(e)) => error(echoJsErrorView, Some(e))
                    case GetViewResponse(_, Right(vd)) =>
                        val modelAndView = new ModelAndView(vd.view, vd.model)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()

                }
            ))
            continuation.undispatch()
        }
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

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            RequestExpiredException()
        } else Option(continuation.getAttribute("json")).getOrElse {
            continuation.suspend(httpServletResponse)

            def resume(json: AnyRef) {
                continuation.setAttribute("json", json)
                continuation.resume()
            }

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
                view = Option(view)).onComplete(_.value.get.fold(
                    e => resume(e),
                    _ match {
                        case RequestEchoResponse(_, Left(e)) => resume(e)
                        case RequestEchoResponse(_, Right(echoPossibilityView)) =>
                            resume(echoPossibilityView)
                    }))

            continuation.undispatch()
        }
    }



    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam(value = "id", required = true) id: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(errorView, RequestExpiredException())
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            val eu = cookieManager.findEchoedUserCookie(httpServletRequest)
            val ec = cookieManager.findEchoClickCookie(httpServletRequest)

            val echoedUserNotFound = LocateWithIdResponse(LocateWithId(eu.orNull), Left(EchoedUserNotFound(eu.orNull)))
            val echoedUserResponse = eu.cata(
                    echoedUserServiceLocator.getEchoedUserServiceWithId(_).recover { case e => echoedUserNotFound },
                    new AlreadyCompletedFuture(Right(echoedUserNotFound)))

            val recordEchoStepResponse  = partnerServiceManager.recordEchoStep(id, "login", eu, ec)

            (for {
                eur <- echoedUserResponse
                resr <- recordEchoStepResponse
            } yield {
                val data = resr.resultOrException

                eur.cata(
                    e => {
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
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()
                    },
                    eus => {
                        logger.debug("Recognized user for echo login {}", eus.id)
                        val modelAndView = new ModelAndView(echoLoginNotNeededView, "id", id)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()
                    }
                )
            }).onException { case e => error(errorView, e) }

            continuation.undispatch()
        }
    }


    @RequestMapping(value = Array("/authorize"), method = Array(RequestMethod.GET))
    def authorize(
            @RequestParam(value = "id", required = true) id: String,
            @RequestParam(value = "network", required = true) network: String,
            @RequestParam(value = "add", required = false, defaultValue = "false") add: Boolean,
            @RequestParam(value = "close", required=false, defaultValue = "false") close: Boolean,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation: Continuation = null //no need for continuation on this one...

        val networkController = networkControllers.get(network)
        val eu= cookieManager.findEchoedUserCookie(httpServletRequest)
        val ec = cookieManager.findEchoClickCookie(httpServletRequest)

        if (networkController == null) {
            error(errorView, EchoedException("Invalid network"))
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
            httpServletResponse: HttpServletResponse) = {

        val modelAndView = new ModelAndView(echoIframe)
        modelAndView

    }
    
    @RequestMapping(value = Array("/close"), method = Array(RequestMethod.GET))
    def close(
            @RequestParam(value = "id", required = true) id: String,
            @RequestParam(value = "network", required = true) network: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        
        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        val eu= cookieManager.findEchoedUserCookie(httpServletRequest)

        if(continuation.isExpired) {
            error(errorView, RequestExpiredException())
        } else Option(continuation.getAttribute("modelAndView")).getOrElse{
            continuation.suspend(httpServletResponse)

            val ec = cookieManager.findEchoClickCookie(httpServletRequest)
            val echoedUserResponse = echoedUserServiceLocator
                .getEchoedUserServiceWithId(eu.get)
                .flatMap(_.resultOrException.getEchoedUser)

            val recordEchoStepResponse  = partnerServiceManager.locatePartnerByEchoId(id).flatMap(_ match {
                case LocateByEchoIdResponse(_, Left(e)) => throw e
                case LocateByEchoIdResponse(_, Right(ps)) => ps.recordEchoStep(id, "confirm", eu, ec)
            })

            (for {
                eur <- echoedUserResponse
                resr <- recordEchoStepResponse
            } yield {
                val eu = eur.resultOrException
                Option(eu.email).getOrElse(None) match {
                    case None =>
                        val modelAndView = new ModelAndView(echoRegisterUrl + "?id=%s&close=true" format id)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()
                    case em =>
                        resr.cata(
                            _ match {
                                case EchoExists(epv, message, _) =>
                                    val modelAndView = new ModelAndView(echoAuthComplete)
                                    modelAndView.addObject("echoedUserName", eu.name)
                                    modelAndView.addObject("facebookUserId", eu.facebookUserId)
                                    modelAndView.addObject("twitterUserId", eu.twitterUserId)
                                    modelAndView.addObject("network", network.capitalize)
                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume()

                                case e =>
                                    val modelAndView = new ModelAndView(echoAuthComplete)
                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume()
                            },
                            epv => {
                                val modelAndView = new ModelAndView(echoAuthComplete)
                                modelAndView.addObject("echoedUserName", eu.name)
                                modelAndView.addObject("facebookUserId", eu.facebookUserId)
                                modelAndView.addObject("twitterUserId", eu.twitterUserId)
                                modelAndView.addObject("network", network.capitalize)
                                continuation.setAttribute("modelAndView", modelAndView)
                                continuation.resume()
                            })
                }
            }).onException {case e=> error(errorView,e)}
            continuation.undispatch()
        }
    }

    @RequestMapping(value = Array("/confirm"), method = Array(RequestMethod.GET))
    def confirm(
            @RequestParam(value = "id", required = true) id: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        val eu= cookieManager.findEchoedUserCookie(httpServletRequest)

        if (eu.isEmpty) {
            error(errorView, EchoedUserNotFound(""))
        } else if (continuation.isExpired) {
            error(errorView, RequestExpiredException())
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            val ec = cookieManager.findEchoClickCookie(httpServletRequest)
            val echoedUserResponse = echoedUserServiceLocator
                    .getEchoedUserServiceWithId(eu.get)
                    .flatMap(_.resultOrException.getEchoedUser)

            val recordEchoStepResponse  = partnerServiceManager.locatePartnerByEchoId(id).flatMap(_ match {
                case LocateByEchoIdResponse(_, Left(e)) => throw e
                case LocateByEchoIdResponse(_, Right(ps)) => ps.recordEchoStep(id, "confirm", eu, ec)
            })

            (for {
                eur <- echoedUserResponse
                resr <- recordEchoStepResponse
            } yield {
                val eu = eur.resultOrException

                Option(eu.email).getOrElse(None) match {
                    case None =>
                        val modelAndView = new ModelAndView(echoRegisterUrl + "?id=%s" format id)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()
                    case em =>
                        resr.cata(
                            _ match {
                                case EchoExists(epv, message, _) =>
                                    logger.debug("Echo possibility already echoed {}", epv.echo)
                                    val modelAndView = new ModelAndView(errorView)
                                    modelAndView.addObject("id", id)
                                    modelAndView.addObject("errorMessage", "This item has already been shared")
                                    modelAndView.addObject("echoPossibilityView", epv)
                                    modelAndView.addObject("maxPercentage", "%1.0f".format(epv.partnerSettings.maxPercentage*100))
                                    modelAndView.addObject("minPercentage", "%1.0f".format(epv.partnerSettings.minPercentage*100))
                                    if(epv.partnerSettings.closetPercentage > 0)
                                        modelAndView.addObject("closetPercentage", "%1.0f".format(epv.partnerSettings.closetPercentage*100))
                                    modelAndView.addObject("partnerLogo", epv.partner.logo)
                                    modelAndView.addObject("numberDays", epv.partnerSettings.creditWindow / 24)
                                    modelAndView.addObject("minClicks", epv.partnerSettings.minClicks);
                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume()
                                case e => error(errorView, e)
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
                                modelAndView.addObject("maxPercentage", "%1.0f".format(epv.partnerSettings.maxPercentage*100));
                                modelAndView.addObject("minPercentage", "%1.0f".format(epv.partnerSettings.minPercentage*100));
                                if(epv.partnerSettings.closetPercentage > 0)
                                    modelAndView.addObject("closetPercentage", "%1.0f".format(epv.partnerSettings.closetPercentage*100))
                                modelAndView.addObject("minClicks", epv.partnerSettings.minClicks);
                                modelAndView.addObject("productPriceFormatted", "%.2f".format(epv.echoPossibility.price));
                                modelAndView.addObject("numberDays", epv.partnerSettings.creditWindow / 24)
                                continuation.setAttribute("modelAndView", modelAndView)
                                continuation.resume()
                            })
                }
            }).onException { case e => error(errorView, e) }

            continuation.undispatch()
        }
    }

    @RequestMapping(value = Array("/finishjson"), method = Array(RequestMethod.GET))
    @ResponseBody
    def finishJSON(
            echoFinishParameters: EchoFinishParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        
        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if(continuation.isExpired) {
            error(errorView, RequestExpiredException("We encountered an error sharing your purchase"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)
            cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoFinishParameters.echoedUserId = _)
            logger.debug("Echoing {}", echoFinishParameters)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoFinishParameters.echoedUserId).onComplete(_.value.get.fold(
                e => error(errorView, e),
                _ match {
                    case LocateWithIdResponse(_, Left(e)) => error(errorView, e)
                    case LocateWithIdResponse(_, Right(echoedUserService)) =>
                        echoedUserService.echoTo(echoFinishParameters.createEchoTo).onComplete(_.value.get.fold(
                            e => error(errorView, e),
                            _ match {
                                case EchoToResponse(_, Left(DuplicateEcho(echo, message, _))) =>
                                    continuation.setAttribute("modelAndView", echo)
                                    continuation.resume()
                                case EchoToResponse(_, Left(e)) => error(errorView,e)
                                case EchoToResponse(_, Right(echoFull)) =>
                                    logger.debug("Received echo response {}", echoFull)
                                    continuation.setAttribute("modelAndView", echoFull)
                                    continuation.resume()
                                    logger.debug("Successfully echoed {}", echoFull)
                            }))
                }))
            continuation.undispatch()
        })
    }


    @RequestMapping(value = Array("/finish"), method = Array(RequestMethod.GET))
    def finish(
            echoFinishParameters: EchoFinishParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(errorView, RequestExpiredException("We encounted an error sharing your purchase"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoFinishParameters.echoedUserId = _)

            logger.debug("Echoing {}", echoFinishParameters)
            echoedUserServiceLocator.getEchoedUserServiceWithId(echoFinishParameters.echoedUserId).onComplete(_.value.get.fold(
                e => error(errorView, e),
                _ match {
                    case LocateWithIdResponse(_, Left(e)) => error(errorView, e)
                    case LocateWithIdResponse(_, Right(echoedUserService)) =>
                        echoedUserService.echoTo(echoFinishParameters.createEchoTo).onComplete(_.value.get.fold(
                            e => error(errorView, e),
                            _ match {
                                case EchoToResponse(_, Left(DuplicateEcho(echo, message, _))) =>
                                    logger.debug("Received duplicate echo response to echo", echo)
                                    continuation.setAttribute("modelAndView", new ModelAndView(echoDuplicateView, "echo", echo))
                                    continuation.resume()
                                case EchoToResponse(_, Left(e)) => error(errorView,e)
                                case EchoToResponse(_, Right(echoFull)) =>
                                    logger.debug("Received echo response {}", echoFull)
                                    continuation.setAttribute("modelAndView", new ModelAndView(echoFinishView, "echoFull", echoFull))
                                    continuation.resume()
                                    logger.debug("Successfully echoed {}", echoFull)
                            }))
                }))

            continuation.undispatch()
        })
    }


    @Deprecated
    @RequestMapping(value = Array("/button"), method = Array(RequestMethod.GET))
    def button(
            @RequestHeader(value = "Referer", required = false) referrerUrl: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            @RequestHeader(value = "User-Agent", required = true) userAgent: String,
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        try {

            cookieManager.findBrowserIdCookie(httpServletRequest).foreach(echoPossibilityParameters.browserId = _)
            echoPossibilityParameters.ipAddress = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr)
            echoPossibilityParameters.userAgent = userAgent
            echoPossibilityParameters.referrerUrl = referrerUrl

            cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoPossibilityParameters.echoedUserId = _)
            cookieManager.findEchoClickCookie(httpServletRequest).foreach(echoPossibilityParameters.echoClickId = _)
            echoService.recordEchoPossibility(echoPossibilityParameters.createButtonEchoPossibility)
        } catch {
            case e => logger.error("Error creating Echo at button step for %s" format echoPossibilityParameters, e)
        }
        new ModelAndView(buttonView)
    }


    @Deprecated
    @RequestMapping(method = Array(RequestMethod.GET))
    def echo(
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoPossibilityParameters.echoedUserId = _)
        cookieManager.findEchoClickCookie(httpServletRequest).foreach(echoPossibilityParameters.echoClickId = _)

        def error(e: Throwable) {
            logger.error("Unexpected error encountered echoing %s" format echoPossibilityParameters, e)
            val modelAndView = new ModelAndView(errorView, "errorMessage", e.getMessage)
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume
            modelAndView
        }

        def confirmEchoPossibility {
            echoedUserServiceLocator.getEchoedUserServiceWithId(echoPossibilityParameters.echoedUserId).onResult {
                case LocateWithIdResponse(_, Left(e)) =>
                    logger.debug("Error {} finding EchoedUserService for {}", e.getMessage, echoPossibilityParameters)
                    loginToEcho
                case LocateWithIdResponse(_, Right(echoedUserService)) =>
                    echoedUserService.getEchoedUser.onResult {
                        case GetEchoedUserResponse(_, Left(e)) => error(e)
                        case GetEchoedUserResponse(_, Right(echoedUser)) =>
                            val echoPossibility = echoPossibilityParameters.createConfirmEchoPossibility

                            echoService.recordEchoPossibility(echoPossibility).onResult {
                                case RecordEchoPossibilityResponse(_, Left(EchoExists(epv, message, _))) =>
                                    logger.debug("Echo possibility already echoed {}", epv.echo)
                                    val modelAndView = new ModelAndView(errorView)
                                    modelAndView.addObject("errorMessage", "This item has already been shared")
                                    modelAndView.addObject("echoPossibilityView", epv)
                                    modelAndView.addObject("partnerLogo", epv.partner.logo)
                                    modelAndView.addObject("maxPercentage", "%1.0f".format(epv.partnerSettings.maxPercentage*100));
                                    modelAndView.addObject("minPercentage", "%1.0f".format(epv.partnerSettings.minPercentage*100));
                                    modelAndView.addObject("minClicks", epv.partnerSettings.minClicks);
                                    modelAndView.addObject("numberDays", epv.partnerSettings.creditWindow / 24)
                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume()
                                case RecordEchoPossibilityResponse(_, Left(e)) => error(e)
                                case RecordEchoPossibilityResponse(_, Right(epv)) =>
                                    val lu = "%s?redirect=%s" format(
                                            logoutUrl,
                                            URLEncoder.encode("echo?" + httpServletRequest.getQueryString.substring(0), "UTF-8"))
                                    val modelAndView = new ModelAndView(confirmView)
                                    modelAndView.addObject("echoedUser", echoedUser)
                                    modelAndView.addObject("echoPossibility", epv.echo)
                                    modelAndView.addObject("partner", epv.partner)
                                    modelAndView.addObject("partnerLogo", epv.partner.logo)
                                    modelAndView.addObject("partnerSettings", epv.partnerSettings)
                                    modelAndView.addObject("logoutUrl", lu)
                                    modelAndView.addObject("maxPercentage", "%1.0f".format(epv.partnerSettings.maxPercentage*100));
                                    modelAndView.addObject("minPercentage", "%1.0f".format(epv.partnerSettings.minPercentage*100));
                                    modelAndView.addObject("minClicks", epv.partnerSettings.minClicks);
                                    modelAndView.addObject("numberDays", epv.partnerSettings.creditWindow / 24)
                                    modelAndView.addObject("productPriceFormatted", "%.2f".format(epv.echoPossibility.price));
                                    modelAndView.addObject(
                                            "facebookAddUrl",
                                            URLEncoder.encode(
                                                    echoPossibility.asUrlParams("%s?redirect=echo?" format facebookAddRedirectUrl),
                                                    "UTF-8"))
                                    modelAndView.addObject(
                                            "twitterAddUrl",
                                            URLEncoder.encode(echoPossibility.asUrlParams("echo?"), "UTF-8"))
                                    continuation.setAttribute("modelAndView", modelAndView)
                                    continuation.resume
                            }.onException { case e => error(e) }
                    }.onException { case e => error(e) }
            }.onException { case e => error(e) }
        }

        def loginToEcho {
            echoService.recordEchoPossibility(echoPossibilityParameters.createLoginEchoPossibility).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case RecordEchoPossibilityResponse(_, Left(e)) => error(e)
                    case RecordEchoPossibilityResponse(_, Right(epv)) =>
                        val modelAndView = new ModelAndView(loginView)
                        modelAndView.addObject(
                            "twitterUrl",
                            URLEncoder.encode(epv.echoPossibility.asUrlParams("echo?"), "UTF-8"))

                        modelAndView.addObject("redirectUrl",
                            URLEncoder.encode(facebookLoginRedirectUrl + "?redirect="
                                + URLEncoder.encode(epv.echoPossibility.asUrlParams("echo?"), "UTF-8"), "UTF-8"))

                        modelAndView.addObject("echoPossibilityView", epv)
                        modelAndView.addObject("partnerLogo", epv.partner.logo)
                        modelAndView.addObject("maxPercentage", "%1.0f".format(epv.partnerSettings.maxPercentage*100));
                        modelAndView.addObject("minPercentage", "%1.0f".format(epv.partnerSettings.minPercentage*100));
                        modelAndView.addObject("productPriceFormatted", "%.2f".format(epv.echoPossibility.price));
                        modelAndView.addObject("minClicks",epv.partnerSettings.minClicks)
                        modelAndView.addObject("numberDays", epv.partnerSettings.creditWindow / 24)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()
                }))
        }


        if (continuation.isExpired) {
            error(RequestExpiredException("We encountered an error echoing your purchase"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            Option(echoPossibilityParameters.echoedUserId).cata(
                _ => confirmEchoPossibility,
                loginToEcho)

            continuation.undispatch()
        })
    }


    @Deprecated
    @RequestMapping(value = Array("/it"), method = Array(RequestMethod.GET))
    def it(
            echoItParameters: EchoItParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {


        cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoItParameters.echoedUserId = _)

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)


        if (continuation.isExpired) {
            error(errorView, RequestExpiredException("We encounted an error echoing your purchase"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            logger.debug("Echoing {}", echoItParameters)
            echoedUserServiceLocator.getEchoedUserServiceWithId(echoItParameters.echoedUserId).onComplete(_.value.get.fold(
                e => error(errorView, e),
                _ match {
                    case LocateWithIdResponse(_, Left(e)) => error(errorView, e)
                    case LocateWithIdResponse(_, Right(echoedUserService)) =>
                        echoedUserService.echoTo(echoItParameters.createEchoTo).onComplete(_.value.get.fold(
                            e => error(errorView, e),
                            _ match {
                                case EchoToResponse(_, Left(DuplicateEcho(echo ,message, _))) =>
                                    logger.debug("Duplicate Echo!")
                                    continuation.setAttribute("modelAndView", new ModelAndView(duplicateView,"echo", echo))
                                    continuation.resume()
                                case EchoToResponse(_, Right(echoFull)) =>
                                    continuation.setAttribute("modelAndView", new ModelAndView(echoItView, "echoFull", echoFull))
                                    continuation.resume()
                                    logger.debug("Successfully echoed {}", echoFull)
                            }))
                }))

            continuation.undispatch()
        })
    }


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

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(errorView, Some(RequestExpiredException()))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            val echoClick = new EchoClick(
                    echoId = null, //we will determine what the echoId is in the service...
                    echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).orNull,
                    browserId = cookieManager.findBrowserIdCookie(httpServletRequest).get,
                    referrerUrl = referrerUrl,
                    ipAddress = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr),
                    userAgent = userAgent)
            val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).orNull
            echoService.recordEchoClick(echoClick, echoId, postId).onComplete(_.value.get.fold(
                e => error(errorView, Some(e)),
                _ match {
                    case RecordEchoClickResponse(msg, Left(e)) => error(errorView, Some(e))
                    case RecordEchoClickResponse(msg, Right(echo)) =>
                        //Add the echoClick tracking cookie if it's not available
                        cookieManager.findEchoClickCookie(httpServletRequest).getOrElse({
                            cookieManager.addEchoClickCookie(
                                    httpServletResponse,
                                    echoClick,
                                    httpServletRequest)
                        })
                        logger.debug("Returned Echo: {}", echo)
                        //continuation.setAttribute("modelAndView", new ModelAndView("redirect:%s" format echo.landingPageUrl))
                        val modelAndView = new ModelAndView(echoRedirectView)
                        modelAndView.addObject("echo", echo)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume()
                        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onComplete(_.value.get.fold(
                            e => logger.debug("Error"),
                            _ match {
                                case LocateWithIdResponse(_, Left(e)) =>
                                case LocateWithIdResponse(_, Right(eus)) =>
                                    logger.debug("Publishing Action: ")
                                    eus.publishFacebookAction("browse","product", productGraphUrl + echoId)
                            }))
                }))
            continuation.undispatch()
        })
    }
}


