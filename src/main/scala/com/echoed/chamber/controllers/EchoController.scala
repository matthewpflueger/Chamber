package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import com.echoed.chamber.services.echo.{RecordEchoClickResponse, EchoExists, RecordEchoPossibilityResponse, EchoService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.continuation.{Continuation, ContinuationSupport}
import com.echoed.chamber.controllers.ControllerUtils._
import akka.dispatch.{AlreadyCompletedFuture, Future}
import com.echoed.chamber.services.echoeduser._
import com.echoed.chamber.services.partner._
import com.echoed.chamber.domain.{EchoedUser, EchoClick}
import java.util.{Map => JMap}
import com.echoed.chamber.services.EchoedException


@Controller
@RequestMapping(Array("/echo"))
class EchoController {

    private final val logger = LoggerFactory.getLogger(classOf[EchoController])

    @BeanProperty var echoJsView: String = _
    @BeanProperty var echoJsErrorView: String = _
    @BeanProperty var echoLoginView: String = _
    @BeanProperty var echoLoginNotNeededView: String = _
    @BeanProperty var echoConfirmView: String = _
    @BeanProperty var echoFinishView: String = _

    @BeanProperty var echoItView: String = _
    @BeanProperty var buttonView: String = _
    @BeanProperty var loginView: String = _
    @BeanProperty var postLoginView: String = _
    @BeanProperty var confirmView: String = _
    @BeanProperty var errorView: String = _
    @BeanProperty var facebookAddRedirectUrl: String = _
    @BeanProperty var facebookLoginRedirectUrl: String = _
    @BeanProperty var logoutUrl: String = _

    @BeanProperty var echoService: EchoService = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var partnerServiceManager: PartnerServiceManager = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var networkControllers: JMap[String, NetworkController] = _


    @RequestMapping(value = Array("/js"), method = Array(RequestMethod.GET), produces = Array("application/x-javascript"))
    def js(
            @RequestParam(value = "pid", required = true) pid: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(echoJsErrorView, Some(RequestExpiredException()))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            partnerServiceManager.locatePartnerService(pid).onComplete(_.value.get.fold(
                e => error(echoJsErrorView, Some(e)),
                _ match {
                    case LocateResponse(_, Left(e)) => error(echoJsErrorView, Some(e))
                    case LocateResponse(_, Right(p)) =>
                        continuation.setAttribute("modelAndView", new ModelAndView(echoJsView, "pid", pid))
                        continuation.resume
                }))

            continuation.undispatch()
        }
    }


    @RequestMapping(value = Array("/request"), method = Array(RequestMethod.GET), produces = Array("application/json"))
    @ResponseBody
    def request(
            @RequestParam(value = "pid", required = true) pid: String,
            @RequestParam(value = "data", required = true) data: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            RequestExpiredException()
        } else Option(continuation.getAttribute("json")).getOrElse {
            continuation.suspend(httpServletResponse)

            def resume(json: AnyRef) {
                continuation.setAttribute("json", json)
                continuation.resume
            }

            val eu= cookieManager.findEchoedUserCookie(httpServletRequest)
            val ec = cookieManager.findEchoClickCookie(httpServletRequest)
            val ip = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr)

            partnerServiceManager.locatePartnerService(pid).onComplete(_.value.get.fold(
                resume(_),
                _ match {
                    case LocateResponse(_, Left(e)) => resume(e)
                    case LocateResponse(_, Right(p)) => p.requestEcho(data, ip, eu, ec).onComplete(_.value.get.fold(
                        e => resume(e),
                        _ match {
                            case RequestEchoResponse(_, Left(e)) => resume(e)
                            case RequestEchoResponse(_, Right(echoPossibilityView)) => resume(echoPossibilityView)
                        }))
                }))

            continuation.undispatch()
        }
    }


    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def login(
            @RequestParam(value = "id", required = true) id: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(errorView, RequestExpiredException())
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            val eu= cookieManager.findEchoedUserCookie(httpServletRequest)
            val ec = cookieManager.findEchoClickCookie(httpServletRequest)
            val ip = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr)

            val echoedUserNotFound = LocateWithIdResponse(LocateWithId(eu.orNull), Left(EchoedUserNotFound(eu.orNull)))
            val echoedUserResponse = eu.cata(
                    echoedUserServiceLocator.getEchoedUserServiceWithId(_).recover { case e => echoedUserNotFound },
                    new AlreadyCompletedFuture(Right(echoedUserNotFound)))

            val recordEchoStepResponse  = partnerServiceManager.locatePartnerByEchoId(id).flatMap(_ match {
                case LocateByEchoIdResponse(_, Left(e)) => throw e
                case LocateByEchoIdResponse(_, Right(ps)) => ps.recordEchoStep(id, "login", ip, eu, ec)
            })

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
                        modelAndView.addObject("maxPercentage", "%1.0f".format(data.retailerSettings.maxPercentage*100));
                        modelAndView.addObject("minPercentage", "%1.0f".format(data.retailerSettings.minPercentage*100));
                        modelAndView.addObject("productPriceFormatted", "%.2f".format(data.echoPossibility.price));
                        modelAndView.addObject("minClicks", data.retailerSettings.minClicks)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume
                    },
                    eus => {
                        logger.debug("Recognized user for echo login {}", eus.id)
                        val modelAndView = new ModelAndView(echoLoginNotNeededView, "id", id)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume
                    }
                )
            }).onException { case e => error(errorView, e) }

            continuation.undispatch
        }
    }


    @RequestMapping(value = Array("/authorize"), method = Array(RequestMethod.GET))
    def authorize(
            @RequestParam(value = "id", required = true) id: String,
            @RequestParam(value = "network", required = true) network: String,
            @RequestParam(value = "add", required = false, defaultValue = "false") add: Boolean,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        val networkController = networkControllers.get(network)

        if (networkController == null) {
            error(errorView, EchoedException("Invalid network"))
        } else if (continuation.isExpired) {
            error(errorView, RequestExpiredException())
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            val eu= cookieManager.findEchoedUserCookie(httpServletRequest)
            val ec = cookieManager.findEchoClickCookie(httpServletRequest)
            val ip = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr)

            partnerServiceManager.locatePartnerByEchoId(id).onComplete(_.value.get.fold(
                e => error(errorView, e),
                _ match {
                    case LocateByEchoIdResponse(_, Left(e)) => error(errorView, e)
                    case LocateByEchoIdResponse(_, Right(ps)) =>
                        ps.recordEchoStep(id, "authorize-%s" format network, ip, eu, ec)
                        val authorizeUrl = networkController.makeAuthorizeUrl("echo/confirm?id=%s" format id, add)
                        logger.debug("Redirecting for authorization to {}", authorizeUrl)
                        val modelAndView = new ModelAndView("redirect:%s" format authorizeUrl)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume
                }))

            continuation.undispatch
        }
    }


    @RequestMapping(value = Array("/confirm"), method = Array(RequestMethod.GET))
    def confirm(
            @RequestParam(value = "id", required = true) id: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
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
            val ip = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr)

            val echoedUserResponse = echoedUserServiceLocator
                    .getEchoedUserServiceWithId(eu.get)
                    .flatMap(_.resultOrException.getEchoedUser)

            val recordEchoStepResponse  = partnerServiceManager.locatePartnerByEchoId(id).flatMap(_ match {
                case LocateByEchoIdResponse(_, Left(e)) => throw e
                case LocateByEchoIdResponse(_, Right(ps)) => ps.recordEchoStep(id, "confirm", ip, eu, ec)
            })

            (for {
                eur <- echoedUserResponse
                resr <- recordEchoStepResponse
            } yield {
                val eu = eur.resultOrException

                resr.cata(
                    _ match {
                        case EchoExists(epv, message, _) =>
                            logger.debug("Echo possibility already echoed {}", epv.echo)
                            val modelAndView = new ModelAndView(errorView)
                            modelAndView.addObject("id", id)
                            modelAndView.addObject("errorMessage", "This item has already been shared")
                            modelAndView.addObject("echoPossibilityView", epv)
                            modelAndView.addObject("maxPercentage", "%1.0f".format(epv.retailerSettings.maxPercentage*100));
                            modelAndView.addObject("minPercentage", "%1.0f".format(epv.retailerSettings.minPercentage*100));
                            modelAndView.addObject("minClicks", epv.retailerSettings.minClicks);
                            continuation.setAttribute("modelAndView", modelAndView)
                            continuation.resume()
                        case e => error(errorView, e)
                    },
                    epv => {
                        val echoPossibility = epv.echoPossibility
                        val lu = "%s?redirect=%s" format(
                                logoutUrl,
                                URLEncoder.encode("echo?" + httpServletRequest.getQueryString.substring(0), "UTF-8"))
                        val modelAndView = new ModelAndView(echoConfirmView)
                        modelAndView.addObject("id", id)
                        modelAndView.addObject("echoedUser", eu)
                        modelAndView.addObject("echoPossibility", echoPossibility)
                        modelAndView.addObject("retailer", epv.retailer)
                        modelAndView.addObject("retailerSettings", epv.retailerSettings)
                        modelAndView.addObject("logoutUrl", lu)
                        modelAndView.addObject("maxPercentage", "%1.0f".format(epv.retailerSettings.maxPercentage*100));
                        modelAndView.addObject("minPercentage", "%1.0f".format(epv.retailerSettings.minPercentage*100));
                        modelAndView.addObject("minClicks", epv.retailerSettings.minClicks);
                        modelAndView.addObject("productPriceFormatted", "%.2f".format(epv.echoPossibility.price));
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume
                    })
            }).onException { case e => error(errorView, e) }

            continuation.undispatch
        }
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
                                case EchoToResponse(_, Left(e)) =>
                                    logger.debug("Received error response to echo", e)
                                    error(errorView, e)
                                case EchoToResponse(_, Right(echoFull)) =>
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
            echoPossibilityParameters: EchoPossibilityParameters,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoPossibilityParameters.echoedUserId = _)
        cookieManager.findEchoClickCookie(httpServletRequest).foreach(echoPossibilityParameters.echoClickId = _)
        echoService.recordEchoPossibility(echoPossibilityParameters.createButtonEchoPossibility)
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
                                    modelAndView.addObject("maxPercentage", "%1.0f".format(epv.retailerSettings.maxPercentage*100));
                                    modelAndView.addObject("minPercentage", "%1.0f".format(epv.retailerSettings.minPercentage*100));
                                    modelAndView.addObject("minClicks", epv.retailerSettings.minClicks);
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
                                    modelAndView.addObject("retailer", epv.retailer)
                                    modelAndView.addObject("retailerSettings", epv.retailerSettings)
                                    modelAndView.addObject("logoutUrl", lu)
                                    modelAndView.addObject("maxPercentage", "%1.0f".format(epv.retailerSettings.maxPercentage*100));
                                    modelAndView.addObject("minPercentage", "%1.0f".format(epv.retailerSettings.minPercentage*100));
                                    modelAndView.addObject("minClicks", epv.retailerSettings.minClicks);
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
                        modelAndView.addObject("maxPercentage", "%1.0f".format(epv.retailerSettings.maxPercentage*100));
                        modelAndView.addObject("minPercentage", "%1.0f".format(epv.retailerSettings.minPercentage*100));
                        modelAndView.addObject("productPriceFormatted", "%.2f".format(epv.echoPossibility.price));
                        modelAndView.addObject("minClicks",epv.retailerSettings.minClicks)
                        continuation.setAttribute("modelAndView", modelAndView)
                        continuation.resume
                }))
        }


        if (continuation.isExpired) {
            error(RequestExpiredException("We encountered an error echoing your purchase"))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            Option(echoPossibilityParameters.echoedUserId).cata(
                _ => confirmEchoPossibility,
                loginToEcho)

            continuation.undispatch
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
                                case EchoToResponse(_, Left(e)) =>
                                    logger.debug("Received error response to echo", e)
                                    error(errorView, e)
                                case EchoToResponse(_, Right(echoFull)) =>
                                    continuation.setAttribute("modelAndView", new ModelAndView(echoItView, "echoFull", echoFull))
                                    continuation.resume()
                                    logger.debug("Successfully echoed {}", echoFull)
                            }))
                }))

            continuation.undispatch()
        })
    }


    @RequestMapping(value = Array("/{echoId}/{postId}"), method = Array(RequestMethod.GET))
    def echoes(
            @PathVariable(value = "echoId") echoId: String,
            @PathVariable(value = "postId") postId: String,
            @RequestHeader(value = "Referer", required = false) referrerUrl: String,
            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
            @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String,
            @RequestHeader(value = "User-Agent", required = false) userAgent: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        implicit val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired) {
            error(errorView, Some(RequestExpiredException()))
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            val echoClick = new EchoClick(
                    echoId,
                    cookieManager.findEchoedUserCookie(httpServletRequest).orNull,
                    referrerUrl,
                    Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr),
                    forwardedFor,
                    userAgent)

            echoService.recordEchoClick(echoClick, postId).onComplete(_.value.get.fold(
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

                        continuation.setAttribute("modelAndView", new ModelAndView("redirect:%s" format echo.landingPageUrl))
                        continuation.resume()
                }))
            continuation.undispatch()
        })
    }
}


