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
import org.springframework.web.bind.annotation._
import org.springframework.web.bind.WebDataBinder
import org.springframework.validation.{Validator , BindingResult }
import javax.validation.Valid
import org.springframework.core.convert.ConversionService
import com.echoed.chamber.controllers.ControllerUtils._
import org.springframework.web.context.request.async.DeferredResult
import java.util.{ArrayList, Date}
import com.echoed.chamber.domain.Echo
import com.echoed.chamber.domain.partner.{Partner, PartnerSettings}

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

        val result = new DeferredResult(new ArrayList[Echo]())

        val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

        adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
            case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                logger.debug("AdminUser Service Located")
                adminUserService.getEchoPossibilities.onSuccess {
                    case GetEchoPossibilitesResponse(_, Right(echoPossibilities)) =>
                        logger.debug("Successfully received Json Response for EchoPossibilities")
                        result.set(echoPossibilities)
                }
        }

        result
    }

    @RequestMapping(value = Array("/partners"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnersJSON(
                        httpServletRequest: HttpServletRequest,
                        httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ArrayList[Partner]())

        val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

        adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
            case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                logger.debug("AdminUser Service Located")
                adminUserService.getPartners.onSuccess {
                    case GetPartnersResponse(_, Right(partners)) =>
                        logger.debug("Received Json Response For Users: {}", partners)
                        result.set(partners)
                }
        }
        result
    }

    @RequestMapping(value = Array("/partner/{partnerId}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerJSON(
                        @PathVariable(value = "partnerId") partnerId: String,
                        httpServletRequest: HttpServletRequest,
                        httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

        adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
            case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                logger.debug("AdminUser Service Located")
                adminUserService.getPartner(partnerId).onSuccess {
                    case GetPartnerResponse(_, Right(partner)) =>
                        logger.debug("Received Json REsponse for Partners: {}", partner)
                        result.set(partner)
                }
        }

        result

    }

    @RequestMapping(value = Array("/partner/{partnerId}/updateHandleAndCategory"), method = Array(RequestMethod.POST))
    @ResponseBody
    def updatePartnerHandleJSON(
            @PathVariable(value = "partnerId") partnerId: String,
            @RequestParam("partnerHandle") partnerHandle: String,
            @RequestParam("partnerCategory") partnerCategory: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

        adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
            case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                logger.debug("AdminUser Service Located")
                adminUserService.updatePartnerHandleAndCategory(partnerId, if( partnerHandle == "" ) null else partnerHandle, partnerCategory).onSuccess {
                    case UpdatePartnerHandleAndCategoryResponse(_, Right(ph)) =>
                        logger.debug("Successfully Update Partner Handle for PartnerId {} with handle {}", partnerId, ph)
                        result.set(ph)
                }
        }
        result
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

        if (bindingResult.hasErrors) {
            "error"
        } else {
            logger.debug("Attempting to update partner settings for partner Id {}", adminUpdatePartnerSettingsForm)

            val result = new DeferredResult("error")
            val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

            adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
                case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                    logger.debug("AdminUser Service Located")
                    adminUpdatePartnerSettingsForm.createPartnerSettings(adminUserService.updatePartnerSettings(_)).onSuccess {
                        case UpdatePartnerSettingsResponse(_, Right(ps)) =>
                            logger.debug("Successfully updated partner settings for partnerId {}", ps.partnerId)
                            result.set(ps)
                    }
            }

            result
        }
    }


    @RequestMapping(value = Array("/partners/{id}/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerSettingsJSON(
            @PathVariable(value = "id") partnerId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")
        val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

        adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
            case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                logger.debug("AdminUser Service Located")
                adminUserService.getPartnerSettings(partnerId).onSuccess {
                    case GetPartnerSettingsResponse(_, Right(partnerSettings)) =>
                        logger.debug("Successfully Received Json Response for Partner Settings for Partner Id {}", partnerId)
                        result.set(partnerSettings)
                }
        }

        result
    }

    @RequestMapping(value = Array("/partners/{id}/settings/current"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getLastPartnerSettingsJSON(
            @PathVariable(value = "id") partnerId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")
        val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

        adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
            case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                logger.debug("AdminUser Service Located")
                adminUserService.getCurrentPartnerSetting(partnerId).onSuccess {
                    case GetCurrentPartnerSettingsResponse(_, Right(partnerSettings)) =>
                        logger.debug("Successfully received Json Response for Partner Settings")
                        result.set(partnerSettings)
                }

        }

        result

    }



    @RequestMapping(value = Array("/users"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getUsersJSON(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
        
        val result = new DeferredResult("error")
        val adminUserId = cookieManager.findAdminUserCookie(httpServletRequest)

        adminUserServiceLocator.locateAdminUserService(adminUserId.get).onSuccess {
            case LocateAdminUserServiceResponse(_, Right(adminUserService)) =>
                logger.debug("AdminUser Service Located")
                adminUserService.getUsers.onSuccess {
                    case GetUsersResponse(_, Right(echoedUsers)) =>
                        logger.debug("Successfully received Json Response For Users")
                        result.set(echoedUsers)
                }
        }

        result
    }

}
