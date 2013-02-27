package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.springframework.web.bind.annotation._
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.chamber.services.echoeduser
import com.echoed.chamber.services.echoeduser._
import com.echoed.chamber.services.partner._
import com.echoed.chamber.domain.EchoClick
import java.util.{Date, Map => JMap}
import java.text.DateFormat
import org.springframework.web.context.request.async.DeferredResult
import javax.annotation.Nullable
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
@RequestMapping(Array("/echo"))
class EchoController extends EchoedController {

//    @BeanProperty var networkControllers: JMap[String, NetworkController] = _
//
//    @RequestMapping(value = Array("/js"), method = Array(RequestMethod.GET), produces = Array("application/x-javascript"))
//    def js(pcc: PartnerClientCredentials) = new ModelAndView(v.echoJsErrorView)
//
//        val result = new DeferredResult(new ModelAndView(v.echoJsErrorView))
//
//        mp(GetView(pcc)).onSuccess {
//            case GetViewResponse(_, Left(e: PartnerNotActive)) =>
//                log.debug("Partner Not Active: Serving Partner Not Active JS template")
//                val modelAndView = new ModelAndView(v.echoJsNotActiveView)
//                modelAndView.addObject("pid", pcc.partnerId)
//                modelAndView.addObject("view", v.echoJsNotActiveView)
//                result.set(modelAndView)
//            case GetViewResponse(_, Right(vd)) =>
//                val modelAndView = new ModelAndView(vd.view, vd.model)
//                result.set(modelAndView)
//        }
//
//        result
//    }
//
//
//    @RequestMapping(value = Array("/request"), method = Array(RequestMethod.GET), produces = Array("application/json"))
//    @ResponseBody
//    def request(
//            @RequestParam(value = "data", required = true) data: String,
//            @RequestParam(value = "view", required = false) view: String,
//            @RequestHeader(value = "Referer", required = false) referrerUrl: String,
//            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
//            @RequestHeader(value = "User-Agent", required = true) userAgent: String,
//            @Nullable eucc: EchoedUserClientCredentials,
//            pcc: PartnerClientCredentials,
//            request: HttpServletRequest) = {
//
//        val result = new DeferredResult(null, ErrorResult.timeout)
//
//        //browser id is required and should be set in the BrowserIdInterceptor...
//        val bi = cookieManager.findBrowserIdCookie(request).get
//        val ec = cookieManager.findEchoClickCookie(request)
//        val ip = Option(remoteIp).getOrElse(request.getRemoteAddr)
//
//        mp(RequestEcho(
//                pcc,
//                request = data,
//                browserId = bi,
//                ipAddress = ip,
//                userAgent = userAgent,
//                referrerUrl = referrerUrl,
//                echoedUserId = Option(eucc).map(_.id),
//                echoClickId = ec,
//                view = Option(view))).onSuccess {
//            case RequestEchoResponse(_, Right(echoPossibilityView)) => result.set(echoPossibilityView)
//        }
//
//        result
//    }
//
//
//
//    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
//    def login(
//            @RequestParam(value = "id", required = true) id: String,
//            @Nullable eucc: EchoedUserClientCredentials,
//            request: HttpServletRequest) = {
//
//        val result = new DeferredResult(null, new ModelAndView(v.errorView))
//
//        val ec = cookieManager.findEchoClickCookie(request)
//
//        mp(RecordEchoStep(id, "login", Option(eucc).map(_.id), ec)).onSuccess {
//            case RecordEchoStepResponse(_, Left(EchoExists(_, _ , _))) =>
//                log.debug("Product already echoed, Redirecting to echo/echoed")
//                result.set(new ModelAndView(v.echoEchoedUrl, "id", id))
//            case RecordEchoStepResponse(_, Right(echoView)) => Option(eucc).cata(
//                _ => result.set(new ModelAndView(v.echoLoginNotNeededView, "id", id)),
//                {
//                    log.debug("Requesting login for echo {}", echoView.echo.id)
//                    val modelAndView = new ModelAndView(v.echoLoginView)
//
//                    modelAndView.addObject("echoPossibilityView", echoView)
//                    modelAndView.addObject("partnerLogo", echoView.partner.logo)
//                    modelAndView.addObject("maxPercentage", "%1.0f".format(echoView.partnerSettings.maxPercentage*100))
//                    //modelAndView.addObject("minPercentage", "%1.0f".format(data.partnerSettings.minPercentage*100));
//
//                    if(echoView.partnerSettings.closetPercentage > 0)
//                        modelAndView.addObject("closetPercentage", "%1.0f".format(echoView.partnerSettings.closetPercentage*100))
//
//                    modelAndView.addObject("numberDays", echoView.partnerSettings.creditWindow / 24)
//                    modelAndView.addObject("productPriceFormatted", "%.2f".format(echoView.echo.price))
//                    modelAndView.addObject("minClicks", echoView.partnerSettings.minClicks)
//                    result.set(modelAndView)
//                })
//        }
//
//        result
//    }
//
//
//    @RequestMapping(value = Array("/authorize"), method = Array(RequestMethod.GET))
//    def authorize(
//            @RequestParam(value = "id", required = true) id: String,
//            @RequestParam(value = "network", required = true) network: String,
//            @RequestParam(value = "add", required = false, defaultValue = "false") add: Boolean,
//            @RequestParam(value = "close", required=false, defaultValue = "false") close: Boolean,
//            @Nullable eucc: EchoedUserClientCredentials,
//            request: HttpServletRequest) = {
//
//        val networkController = networkControllers.get(network)
//        val ec = cookieManager.findEchoClickCookie(request)
//
//        mp(RecordEchoStep(id, "authorize-%s" format network, Option(eucc).map(_.id), ec))
//        val authorizeUrl =
//            if (close) networkController.makeAuthorizeUrl("echo/close?id=%s&network=%s" format (id, network), add)
//            else networkController.makeAuthorizeUrl("echo/confirm?id=%s" format id, add)
//        log.debug("Redirecting for authorization to {}", authorizeUrl)
//        new ModelAndView("redirect:%s" format authorizeUrl)
//
//    }


    @RequestMapping(value = Array("iframe"), method = Array(RequestMethod.GET))
    def iframe() = new ModelAndView(v.echoIframe)

    
//    @RequestMapping(value = Array("/close"), method = Array(RequestMethod.GET))
//    def close(
//            @RequestParam(value = "id", required = true) id: String,
//            @RequestParam(value = "network", required = true) network: String,
//            eucc: EchoedUserClientCredentials,
//            request: HttpServletRequest) = {
//
//        val result = new DeferredResult(null, new ModelAndView(v.errorView))
//
//        val ec = cookieManager.findEchoClickCookie(request)
//
//        mp(RecordEchoStep(id, "confirm", Some(eucc.id), ec))
//
//        mp(GetEchoedUser(eucc)).onSuccess {
//            case GetEchoedUserResponse(_, Right(eu)) =>
//                val modelAndView = new ModelAndView(v.echoAuthComplete)
//                modelAndView.addObject("echoedUserName", eu.name)
//                modelAndView.addObject("facebookUserId", eu.facebookUserId)
//                modelAndView.addObject("twitterUserId", eu.twitterUserId)
//                modelAndView.addObject("network", network.capitalize)
//                result.set(modelAndView)
//        }
//
//        result
//    }
//
//
//    @RequestMapping(value = Array("/confirm"), method = Array(RequestMethod.GET))
//    def confirm(
//            @RequestParam(value = "id", required = true) id: String,
//            eucc: EchoedUserClientCredentials,
//            request: HttpServletRequest) = {
//
//        val result = new DeferredResult(null, new ModelAndView(v.errorView))
//
//        val ec = cookieManager.findEchoClickCookie(request)
//
//        mp(RecordEchoStep(id, "confirm", Some(eucc.id), ec)).onSuccess {
//            case RecordEchoStepResponse(_, Left(EchoExists(epv, message , _))) =>
//                log.debug("Product already echoed {}, Redirecting to echo/echoed", epv)
//                result.set(new ModelAndView(v.echoEchoedUrl, "id", id))
//            case RecordEchoStepResponse(_, Right(epv)) =>
//                val lu = "%s?redirect=%s" format(
//                        v.logoutUrl,
//                        URLEncoder.encode("echo/login?" + request.getQueryString.substring(0), "UTF-8"))
//                val modelAndView = new ModelAndView(v.echoConfirmView)
//                modelAndView.addObject("id", id)
//                modelAndView.addObject("echoedUser", eucc)
//                modelAndView.addObject("echoPossibility", epv.echo)
//                modelAndView.addObject("partner", epv.partner)
//                modelAndView.addObject("partnerSettings", epv.partnerSettings)
//                modelAndView.addObject("logoutUrl", lu)
//                modelAndView.addObject("partnerLogo", epv.partner.logo)
//                modelAndView.addObject("maxPercentage", "%1.0f".format(epv.partnerSettings.maxPercentage*100))
//                modelAndView.addObject("minPercentage", "%1.0f".format(epv.partnerSettings.minPercentage*100))
//                if(epv.partnerSettings.closetPercentage > 0)
//                    modelAndView.addObject("closetPercentage", "%1.0f".format(epv.partnerSettings.closetPercentage*100))
//                modelAndView.addObject("minClicks", epv.partnerSettings.minClicks)
//                modelAndView.addObject("productPriceFormatted", "%.2f".format(epv.echo.price))
//                modelAndView.addObject("numberDays", epv.partnerSettings.creditWindow / 24)
//
//                result.set(modelAndView)
//        }
//
//        result
//    }
//
//
//    @RequestMapping(value = Array("/finishjson"), method = Array(RequestMethod.GET))
//    @ResponseBody
//    def finishJSON(
//            echoFinishParameters: EchoFinishParameters,
//            eucc: EchoedUserClientCredentials) = {
//
//        val result = new DeferredResult(null, ErrorResult.timeout)
//
//        log.debug("Echoing {}", echoFinishParameters)
//
//        mp(echoFinishParameters.createEchoTo(eucc)).onSuccess {
//            case EchoToResponse(_, Left(DuplicateEcho(echo, message, _))) =>
//                result.set(echo)
//            case EchoToResponse(_, Right(echoFull)) =>
//                log.debug("Received echo response {}", echoFull)
//                result.set(echoFull)
//                log.debug("Successfully echoed {}", echoFull)
//        }
//
//        result
//    }
//
//
//    @RequestMapping(value = Array("/finish"), method = Array(RequestMethod.GET))
//    def finish(
//            echoFinishParameters: EchoFinishParameters,
//            eucc: EchoedUserClientCredentials) = {
//
//        log.debug("Echoing {}", echoFinishParameters)
//        mp(echoFinishParameters.createEchoTo(eucc))
//        new ModelAndView(v.echoEchoedUrl, "id", echoFinishParameters.getEchoId())
//    }
//
//
//    @RequestMapping(value = Array("/echoed"), method=Array(RequestMethod.GET))
//    def echoed(
//            @RequestParam(value = "id", required = true) echoId: String,
//            eucc: EchoedUserClientCredentials) = {
//
//        val result = new DeferredResult(null, new ModelAndView(v.errorView))
//
//        mp(echoeduser.GetEcho(eucc, echoId)).onSuccess {
//            case echoeduser.GetEchoResponse(_, Right((echo, echoedUser, partner))) =>
//                if (!echoedUser.hasEmail) result.set(new ModelAndView(v.echoRegisterUrl + "?id=%s" format echoId))
//                else {
//                    val modelAndView = new ModelAndView(v.echoFinishView)
//                    modelAndView.addObject("echo", echo)
//                    modelAndView.addObject("partner", partner)
//                    modelAndView.addObject("echoedUser", echoedUser)
//                    result.set(modelAndView)
//                }
//        }
//
//        result
//    }
//
//
//    @RequestMapping(value = Array("/{postId}"), method = Array(RequestMethod.GET))
//    def click(
//            @PathVariable(value = "postId") postId: String,
//            @RequestHeader(value = "Referer", required = false) referrerUrl: String,
//            @RequestHeader(value = "X-Real-IP", required = false) remoteIp: String,
//            @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String,
//            @RequestHeader(value = "User-Agent", required = false) userAgent: String,
//            @Nullable eucc: EchoedUserClientCredentials,
//            httpServletRequest: HttpServletRequest,
//            httpServletResponse: HttpServletResponse) = {
//
//        val result = new DeferredResult(null, new ModelAndView(v.errorView))
//        val echoedUserId = Option(eucc).map(_.id).orNull
//
//        val echoClick = new EchoClick(
//                echoId = null, //we will determine what the echoId is in the service...
//                echoedUserId = echoedUserId,
//                browserId = cookieManager.findBrowserIdCookie(httpServletRequest).get,
//                referrerUrl = referrerUrl,
//                ipAddress = Option(remoteIp).getOrElse(httpServletRequest.getRemoteAddr),
//                userAgent = userAgent)
//
//        mp(RecordEchoClick(postId, echoClick)).onSuccess {
//            case RecordEchoClickResponse(msg, Right(epv)) =>
//                log.debug("Got echo possibility view for echo {}", postId)
//                cookieManager.addEchoClickCookie(
//                    httpServletResponse,
//                    echoClick,
//                    httpServletRequest)
//
//                val partnerSettings = epv.partnerSettings
//                val partner = epv.partner
//                log.debug("Returned Echo: {}", epv.echo)
//
//                if (partnerSettings.couponCode != null && partnerSettings.couponExpiresOnDate.after(new Date())) {
//                    log.debug("Coupon Code!")
//                    val modelAndView = new ModelAndView(v.echoCouponView)
//                    modelAndView.addObject("echo", epv.echo)
//                    modelAndView.addObject("partnerSettings", partnerSettings)
//                    modelAndView.addObject("partner", partner)
//                    val df = DateFormat.getDateInstance(DateFormat.MEDIUM)
//                    modelAndView.addObject("couponExpiresOn", df.format(partnerSettings.couponExpiresOn))
//                    result.set(modelAndView)
//                } else {
//                    log.debug("No Coupon Code!")
//                    val modelAndView = new ModelAndView(v.echoRedirectView)
//                    modelAndView.addObject("echo", epv.echo)
//                    result.set(modelAndView)
//                }
//
//                Option(eucc).map(c => mp(PublishFacebookAction(c, "browse", "product", v.productGraphUrl + postId)))
//        }
//
//        result
//    }
//}
//
//case class EchoFinishParameters(
//        @BeanProperty var message: String = null,
//        @BeanProperty var postToFacebook: Boolean = false,
//        @BeanProperty var postToTwitter: Boolean = false,
//        @BeanProperty var echoId: String = null) {
//
//    def this() = this(
//            null,
//            false,
//            false,
//            null)
//
//    def createEchoTo(eucc: EchoedUserClientCredentials) = new EchoTo(
//            eucc,
//            echoId,
//            Option(message),
//            postToFacebook,
//            Option(message),
//            postToTwitter)
}
