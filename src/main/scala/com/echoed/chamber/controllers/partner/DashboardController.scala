package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.controllers.{FormController, Errors, EchoedController}
import com.echoed.chamber.controllers.interceptors.Secure
import javax.validation.Valid
import org.springframework.validation.BindingResult
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}


@Controller
@Secure(redirectToPath = "/partner/login")
class DashboardController extends EchoedController with FormController {

    @RequestMapping(value = Array("/partner/dashboard"), method = Array(RequestMethod.GET))
    def dashboard(pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult(new ModelAndView(v.partnerDashboardErrorView))

        log.debug("Showing dashboard for {}", pucc)
        mp(GetPartnerUser(pucc)).onSuccess {
            case GetPartnerUserResponse(_, Right(pu)) =>
                log.debug("Got {}", pu)
                result.set(new ModelAndView(v.partnerDashboardView, "partnerUser", pu))
        }

        result
    }

    @RequestMapping(value = Array("/partner/partneruser"), method = Array(RequestMethod.GET))
    def updatePartnerUserGet(pucc: PartnerUserClientCredentials) =
            new ModelAndView(v.updatePartnerUserView, "updatePartnerUserForm", new UpdatePartnerUserForm(pucc))


    @RequestMapping(value = Array("/partner/partneruser"), method = Array(RequestMethod.POST))
    def registerPost(
            @Valid form: UpdatePartnerUserForm,
            bindingResult: BindingResult,
            request: HttpServletRequest,
            response: HttpServletResponse,
            pucc: PartnerUserClientCredentials) = {


        val errorModelAndView = new ModelAndView(v.updatePartnerUserView) with Errors

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult(errorModelAndView)

            mp(UpdatePartnerUser(pucc, form.name, form.email, form.password)).onSuccess {
                case UpdatePartnerUserResponse(_, Right(partnerUser)) =>
                    cookieManager.addPartnerUserCookie(response, partnerUser, request)
                    result.set(new ModelAndView(v.postUpdatePartnerUserView))
            }

            result
        }
    }
}
