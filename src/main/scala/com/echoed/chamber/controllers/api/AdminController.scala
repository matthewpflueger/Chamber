package com.echoed.chamber.controllers.api

import com.echoed.chamber.controllers.api.admin.{AdminUpdatePartnerSettingsFormValidator, AdminUpdatePartnerSettingsForm}
import org.springframework.stereotype.Controller
import reflect.BeanProperty
import com.echoed.chamber.services.adminuser._
import com.echoed.chamber.controllers.{FormController, EchoedController, ErrorResult}
import org.springframework.web.bind.annotation._
import org.springframework.validation.BindingResult
import javax.validation.Valid
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.state._
import com.echoed.chamber.controllers.api.admin.AdminUpdatePartnerSettingsForm
import com.echoed.chamber.services.adminuser.GetPartnerSettingsResponse
import com.echoed.chamber.services.state.QueryStoriesForAdminResponse
import com.echoed.chamber.services.state.QueryPartnersResponse
import com.echoed.chamber.services.adminuser.GetUsersResponse
import com.echoed.chamber.services.adminuser.UpdatePartnerHandleAndCategory
import com.echoed.chamber.services.adminuser.UpdatePartnerHandleAndCategoryResponse
import scala.Right
import com.echoed.chamber.services.adminuser.GetPartner
import com.echoed.chamber.services.adminuser.GetCurrentPartnerSettings
import com.echoed.chamber.services.adminuser.GetCurrentPartnerSettingsResponse
import com.echoed.chamber.services.adminuser.UpdatePartnerSettings
import com.echoed.chamber.services.state.QueryStoriesForAdmin
import com.echoed.chamber.services.adminuser.GetPartnerResponse
import com.echoed.chamber.services.adminuser.GetPartnerSettings
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials
import com.echoed.chamber.services.adminuser.UpdatePartnerSettingsResponse
import com.echoed.chamber.services.adminuser.GetUsers
import com.echoed.chamber.services.state.QueryPartners


@Controller
@RequestMapping(Array("/admin"))
class AdminController extends EchoedController with FormController {

    @BeanProperty var formValidator: AdminUpdatePartnerSettingsFormValidator = _


    @RequestMapping(value = Array("/stories"), method = Array(RequestMethod.GET))
    @ResponseBody
    def queryStories(
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "pageSize", required = false, defaultValue = "30") pageSize: Int,
            @RequestParam(value = "moderated", required = false) moderated: String,
            aucc: AdminUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(QueryStoriesForAdmin(aucc, page, pageSize, Option(moderated).map(_.toBoolean))).onSuccess {
            case QueryStoriesForAdminResponse(_, Right(stories)) =>
                result.set(stories)
        }

        result
    }

    @RequestMapping(value = Array("/partners"), method = Array(RequestMethod.GET))
    @ResponseBody
    def queryPartners(
            aucc: AdminUserClientCredentials,
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "pageSize", required = false, defaultValue = "30") pageSize: Int) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(QueryPartnersAndPartnerUsers(aucc, page, pageSize)).onSuccess {
            case QueryPartnersAndPartnerUsersResponse(_, Right(partners)) =>
                result.set(partners)
        }

        result
    }

    @RequestMapping(value = Array("/partnerusers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def queryPartnerUsers(
            aucc: AdminUserClientCredentials,
            @RequestParam(value = "partnerId", required = true) partnerId: String,
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "pageSize", required = false, defaultValue = "30") pageSize: Int) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(QueryPartnerUsers(aucc, partnerId, page, pageSize)).onSuccess {
            case QueryPartnerUsersResponse(_, Right(partnerUsers)) =>
                result.set(partnerUsers)
        }

        result
    }

    @RequestMapping(value = Array("/echoedusers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def queryEchoedUsers(
            aucc: AdminUserClientCredentials,
            @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
            @RequestParam(value = "pageSize", required = false, defaultValue = "30") pageSize: Int) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(QueryEchoedUsersForAdmin(aucc, page, pageSize)).onSuccess {
            case QueryEchoedUsersForAdminResponse(_, Right(echoedUsers)) =>
                result.set(echoedUsers)
        }

        result

    }

    @RequestMapping(value = Array("/partner/{partnerId}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerJSON(
            @PathVariable(value = "partnerId") partnerId: String,
            aucc: AdminUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetPartner(aucc, partnerId)).onSuccess {
            case GetPartnerResponse(_, Right(partner)) =>
                log.debug("Received Json REsponse for Partners: {}", partner)
                result.set(partner)
        }

        result
    }

    @RequestMapping(value = Array("/partner/{partnerId}/updateHandleAndCategory"), method = Array(RequestMethod.POST))
    @ResponseBody
    def updatePartnerHandleJSON(
            @PathVariable(value = "partnerId") partnerId: String,
            @RequestParam("partnerHandle") partnerHandle: String,
            @RequestParam("partnerCategory") partnerCategory: String,
            aucc: AdminUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(UpdatePartnerHandleAndCategory(
                aucc,
                partnerId,
                Option(partnerHandle).map(_.trim).filter(_.length > 0).orNull,
                partnerCategory)).onSuccess {
            case UpdatePartnerHandleAndCategoryResponse(_, Right(ph)) =>
                log.debug("Successfully Update Partner Handle for PartnerId {} with handle {}", partnerId, ph)
                result.set(ph)
        }

        result
    }

    @RequestMapping(value = Array("/partners/{id}/settings/update"), method = Array(RequestMethod.POST))
    @ResponseBody
    def updatePartnerSettingsJSON(
            @Valid adminUpdatePartnerSettingsForm: AdminUpdatePartnerSettingsForm,
            aucc: AdminUserClientCredentials,
            bindingResult: BindingResult) = {

        if (!bindingResult.hasErrors) {
            formValidator.validate(adminUpdatePartnerSettingsForm, bindingResult)
        }

        if (bindingResult.hasErrors) {
            ErrorResult("error")
        } else {
            log.debug("Attempting to update partner settings for partner Id {}", adminUpdatePartnerSettingsForm)

            val result = new DeferredResult(ErrorResult.timeout)

            adminUpdatePartnerSettingsForm.createPartnerSettings(ps => mp(UpdatePartnerSettings(aucc, ps)).onSuccess {
                case UpdatePartnerSettingsResponse(_, Right(ps)) =>
                    log.debug("Successfully updated partner settings for partnerId {}", ps.partnerId)
                    result.set(ps)
            })

            result
        }
    }


    @RequestMapping(value = Array("/partners/{id}/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerSettingsJSON(
            @PathVariable(value = "id") partnerId: String,
            aucc: AdminUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetPartnerSettings(aucc, partnerId)).onSuccess {
            case GetPartnerSettingsResponse(_, Right(partnerSettings)) =>
                log.debug("Successfully Received Json Response for Partner Settings for Partner Id {}", partnerId)
                result.set(partnerSettings)
        }

        result
    }

    @RequestMapping(value = Array("/partners/{id}/settings/current"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getLastPartnerSettingsJSON(
            @PathVariable(value = "id") partnerId: String,
            aucc: AdminUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetCurrentPartnerSettings(aucc, partnerId)).onSuccess {
            case GetCurrentPartnerSettingsResponse(_, Right(partnerSettings)) =>
                log.debug("Successfully received Json Response for Partner Settings")
                result.set(partnerSettings)
        }

        result
    }



    @RequestMapping(value = Array("/users"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getUsersJSON(aucc: AdminUserClientCredentials) = {
        val result = new DeferredResult("error")

        mp(GetUsers(aucc)).onSuccess {
            case GetUsersResponse(_, Right(echoedUsers)) =>
                log.debug("Successfully received Json Response For Users")
                result.set(echoedUsers)
        }

        result
    }

}
