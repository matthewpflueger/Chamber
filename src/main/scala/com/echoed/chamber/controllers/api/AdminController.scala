package com.echoed.chamber.controllers.api

import admin.AdminUpdatePartnerSettingsForm
import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.facebook.{FacebookService, FacebookServiceLocator}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserServiceLocator}
import com.echoed.chamber.services.adminuser._
import com.echoed.chamber.controllers.CookieManager
import com.echoed.chamber.domain.partner.PartnerSettings
import java.util.Date
import org.springframework.web.bind.annotation._
import org.springframework.web.bind.WebDataBinder
import org.springframework.validation.{Validator , BindingResult }
import javax.validation.Valid
import org.springframework.core.convert.ConversionService
import com.echoed.chamber.controllers.ControllerUtils._

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/7/12
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(Array("/admin"))
class AdminController {
    
    private val logger = LoggerFactory.getLogger(classOf[AdminController])
    
    @BeanProperty var adminUserServiceLocator: AdminUserServiceLocator = _

    @BeanProperty var globalValidator: Validator = _
    @BeanProperty var formValidator: Validator = _
    @BeanProperty var conversionService: ConversionService = _


    @BeanProperty var cookieManager: CookieManager = _

    @InitBinder
    def initBinder(binder: WebDataBinder) {
        binder.setFieldDefaultPrefix("registerForm.")
        binder.setValidator(globalValidator)
        binder.setConversionService(conversionService)
    }


    @RequestMapping(value = Array("/echoPossibility"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getEchoPossibilityJSON(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(continuation.isExpired){
            logger.error("Request expired getting echoPossibilities via admin api")
            continuation.setAttribute("jsonResponse", "error")
            continuation.resume()
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

            adminUserServiceLocator.locateAdminUserService(adminUserId.get).onResult({
                case LocateAdminUserServiceResponse(_, Left(e)) =>
                    continuation.setAttribute("jsonResponse","error")
                    continuation.resume()
                case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                    logger.debug("AdminUser Service Located")
                    adminUserService.getEchoPossibilities.onResult({
                        case GetEchoPossibilitesResponse(_, Left(e)) =>
                            continuation.setAttribute("jsonResponse","error")
                            continuation.resume()
                        case GetEchoPossibilitesResponse(_, Right(echoPossibilities)) =>
                            continuation.setAttribute("jsonResponse", echoPossibilities)
                            continuation.resume()
                    })
            })

            continuation.undispatch()
            
        })
    }

    @RequestMapping(value = Array("/partners"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnersJSON(
                        httpServletRequest: HttpServletRequest,
                        httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if(continuation.isExpired){
            logger.error("Request expired getting users via admin api")
            continuation.setAttribute("jsonResponse","error")
            continuation.resume()
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

            adminUserServiceLocator.locateAdminUserService(adminUserId.get).onResult({
                case LocateAdminUserServiceResponse(_, Left(e)) =>
                    continuation.setAttribute("jsonResponse","error")
                    continuation.resume()
                case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                    logger.debug("AdminUser Service Located")
                    adminUserService.getPartners.onResult({
                        case GetPartnersResponse(_, Left(e)) =>
                            continuation.setAttribute("jsonResponse", "error")
                            continuation.resume()
                        case GetPartnersResponse(_, Right(partners)) =>
                            logger.debug("Received Json Response For Users: {}",partners)
                            continuation.setAttribute("jsonResponse",partners)
                            continuation.resume()
                    })
            })
            continuation.undispatch()
        })

    }

    @RequestMapping(value = Array("/partners/{id}/settings/update"), method = Array(RequestMethod.POST))
    @ResponseBody
    def updatePartnerSettingsJSON(
            @Valid adminUpdatePartnerSettingsForm: AdminUpdatePartnerSettingsForm,
            bindingResult: BindingResult,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {


        if (!bindingResult.hasErrors) {
            formValidator.validate(adminUpdatePartnerSettingsForm, bindingResult)
        }


        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        logger.debug("Attempting to update partner settings for partner Id {}", adminUpdatePartnerSettingsForm)

        if (continuation.isExpired) {
            logger.error("Request expired updating Partner Setings via Admin Api")
            continuation.setAttribute("jsonResponse", "error")
            continuation.resume()
        } else if (bindingResult.hasErrors) {
            "error"
            //continuation.resume()
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({

            val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

            continuation.suspend(httpServletResponse)

            adminUserServiceLocator.locateAdminUserService(adminUserId.get).onResult({
                case LocateAdminUserServiceResponse(_, Left(e)) =>
                    continuation.setAttribute("jsonResponse", "error")
                    continuation.resume()
                case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                    logger.debug("AdminUser Service Located")
                    adminUpdatePartnerSettingsForm.createPartnerSettings(adminUserService.updatePartnerSettings(_)).onComplete(_.value.get.fold(
                        e => continuation.setAttribute("jsonResponse", e),
                        _ match {
                            case UpdatePartnerSettingsResponse(_, Left(e)) =>
                                continuation.setAttribute("jsonResponse", e)
                                continuation.resume()
                            case UpdatePartnerSettingsResponse(_, Right(ps)) =>
                                continuation.setAttribute("jsonResponse", ps)
                                continuation.resume()
                        }))
            })
            continuation.undispatch()
        })
    }


    @RequestMapping(value = Array("/partners/{id}/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerSettingsJSON(
            @PathVariable(value = "id") partnerId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if (continuation.isExpired){
            logger.error("Request expired getting users via admin api")
            continuation.setAttribute("jsonResponse","error")
            continuation.resume()
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({

            continuation.suspend(httpServletResponse)

            val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

            adminUserServiceLocator.locateAdminUserService(adminUserId.get).onResult({
                case LocateAdminUserServiceResponse(_, Left(e)) =>
                    continuation.setAttribute("jsonResponse","error")
                    continuation.resume()
                case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                    logger.debug("AdminUser Service Located")
                    adminUserService.getPartnerSettings(partnerId).onResult({
                        case GetPartnerSettingsResponse(_, Left(e)) =>
                            continuation.setAttribute("jsonResponse", "error")
                            continuation.resume()
                        case GetPartnerSettingsResponse(_, Right(partnerSettings)) =>
                            logger.debug("Received Json Response For Partner Settings: {}",partnerSettings)
                            continuation.setAttribute("jsonResponse",partnerSettings)
                            continuation.resume()
                    })
            })
            continuation.undispatch()
        })

    }



    @RequestMapping(value = Array("/users"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getUsersJSON(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        
        if(continuation.isExpired){
            logger.error("Request expired getting users via admin api")
            continuation.setAttribute("jsonResponse","error")
            continuation.resume()
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

            adminUserServiceLocator.locateAdminUserService(adminUserId.get).onResult({
                case LocateAdminUserServiceResponse(_, Left(e)) => 
                    continuation.setAttribute("jsonResponse","error")
                    continuation.resume()
                case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                    logger.debug("AdminUser Service Located")
                    adminUserService.getUsers.onResult({
                        case GetUsersResponse(_, Left(e)) => 
                            continuation.setAttribute("jsonResponse", "error")
                            continuation.resume()
                        case GetUsersResponse(_, Right(echoedUsers)) =>
                            logger.debug("Received Json Response For Users: {}",echoedUsers)
                            continuation.setAttribute("jsonResponse",echoedUsers)
                            continuation.resume()
                    })
            })
            continuation.undispatch()
        })

    }

}
