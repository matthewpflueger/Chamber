package com.echoed.chamber.controllers.shopify

import org.springframework.stereotype.Controller
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.shopify.ShopifyUserServiceLocator
import com.echoed.chamber.services.shopify.{LocateByTokenResponse, GetShopifyUserResponse}
import com.echoed.chamber.controllers.CookieManager
import com.echoed.chamber.services.partner.PartnerServiceManager
import com.echoed.chamber.services.partner.{RegisterPartnerResponse, PartnerNotActive, PartnerAlreadyExists}
import com.echoed.chamber.domain.{Partner, PartnerSettings, PartnerUser}
import com.echoed.chamber.services.partneruser.{GetPartnerUserResponse, LoginResponse, PartnerUserServiceLocator}
import com.echoed.util.mustache.MustacheEngine


@Controller
@RequestMapping(Array("/shopify"))
class ShopifyController {

    @BeanProperty var shopifyUserServiceLocator: ShopifyUserServiceLocator = _
    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _
    @BeanProperty var partnerServiceManager: PartnerServiceManager = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var shopifyIntegrationView: String = _
    @BeanProperty var partnerLoginView: String = _
    @BeanProperty var mustacheEngine: MustacheEngine = _

    private val logger = LoggerFactory.getLogger(classOf[ShopifyController])

    @RequestMapping(value = Array("/auth"), method = Array(RequestMethod.GET))
    def auth(
                @RequestParam(value = "shop", required = true) shop: String,
                @RequestParam(value = "signature", required = true) signature: String,
                @RequestParam(value = "t", required = true) t: String,
                @RequestParam(value = "timestamp", required = true) timeStamp: String,
                httpServletRequest: HttpServletRequest,
                httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        def error(e: Throwable) = {
            val modelAndView = new ModelAndView("ERROR")
            continuation.setAttribute("modelAndView", modelAndView)
            continuation.resume()
            modelAndView
        }
        if (continuation.isExpired) {
            logger.debug("Continuation is expired")
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)

            logger.debug("Attempting to locate Shopify User Service: {} ", shop)
            shopifyUserServiceLocator.locate(shop, signature, t, timeStamp).onComplete(_.value.get.fold(
                error(_),
                _ match {
                    case LocateByTokenResponse(_, Left(e)) => error(e)
                    case LocateByTokenResponse(_, Right(shopifyUserService)) =>
                        logger.debug("Received shopify user service")

                        shopifyUserService.getShopifyUser.onComplete(_.value.get.fold(
                            error(_),
                            _ match {
                                case GetShopifyUserResponse(_, Left(e)) => error(e)
                                case GetShopifyUserResponse(_, Right(shopifyUser)) =>
                                    val partner = new Partner(shopifyUser)
                                    val partnerSettings = PartnerSettings.createFuturePartnerSettings(shopifyUser.partnerId)
                                    var partnerUser = new PartnerUser(shopifyUser)
                                    partnerUser = partnerUser.createPassword(shopifyUser.partnerId)
                                    partnerServiceManager.registerPartner(partner,partnerSettings,partnerUser).onComplete(_.value.get.fold(
                                        error(_),
                                        _ match {
                                            case RegisterPartnerResponse(_, Right(pu)) =>
                                                val modelAndView = new ModelAndView(shopifyIntegrationView)
                                                modelAndView.addObject("partner", partner)
                                                modelAndView.addObject("partnerSettings", partnerSettings)
                                                modelAndView.addObject("partnerUser", partnerUser)
                                                continuation.setAttribute("modelAndView", modelAndView)
                                                continuation.resume()
                                            case RegisterPartnerResponse(_, Left(e: PartnerNotActive)) =>
                                                val modelAndView = new ModelAndView(shopifyIntegrationView)
                                                modelAndView.addObject("partner", partner)
                                                modelAndView.addObject("partnerSettings", partnerSettings)
                                                modelAndView.addObject("partnerUser", partnerUser)
                                                continuation.setAttribute("modelAndView", modelAndView)
                                                continuation.resume();
                                            case RegisterPartnerResponse(_, Left(e: PartnerAlreadyExists)) =>
                                                //IF THE PARTNER ALREADY EXISTS LOG THEM IN
                                                logger.debug("Partner Already Exists with Partner User: {}", e.partnerUser)
                                                partnerUserServiceLocator.login(shopifyUser.email, shopifyUser.partnerId).onComplete(_.value.get.fold(
                                                    error(_),
                                                    _ match {
                                                        case LoginResponse(_, Left(e)) =>
                                                        case LoginResponse(_, Right(partnerUserService)) => partnerUserService.getPartnerUser.onComplete(_.value.get.fold(
                                                            error(_),
                                                            _ match{
                                                                case GetPartnerUserResponse(_, Left(e2)) => error(e2)
                                                                case GetPartnerUserResponse(_, Right(pu)) =>
                                                                    cookieManager.addPartnerUserCookie(httpServletResponse, pu, httpServletRequest)
                                                                    continuation.setAttribute("modelAndView", new ModelAndView(partnerLoginView))
                                                                    continuation.resume()
                                                            }))

                                                    }
                                                ))
                                            case RegisterPartnerResponse(_, Left(e)) => error(e)
                                        }))
                            }))
                }))
            continuation.undispatch()
        }
    }


}
