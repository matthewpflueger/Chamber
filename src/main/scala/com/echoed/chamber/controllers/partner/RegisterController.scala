package com.echoed.chamber.controllers.partner

import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import com.echoed.chamber.services.partner._
import javax.validation.Valid
import org.springframework.validation.BindingResult
import com.echoed.chamber.controllers.{FormController, EchoedController, Errors}
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.controllers.interceptors.Secure
import scala.collection.JavaConversions._
import com.echoed.util.ScalaObjectMapper
import com.echoed.chamber.services.feed.GetCommunitiesResponse
import scala.Left
import com.echoed.chamber.services.partner.RegisterPartner
import com.echoed.chamber.domain.views.CommunityFeed
import scala.Right
import scala.Some
import com.echoed.chamber.services.feed.GetCommunities


@Controller
@Secure
class RegisterController extends EchoedController with FormController {

    @RequestMapping(value = Array("/partner/register"), method = Array(RequestMethod.GET))
    def registerGet = {

        val result = new DeferredResult[ModelAndView](null, "error")

        mp(GetCommunities()).onSuccess {
            case GetCommunitiesResponse(_, Right(CommunityFeed(list))) =>
                val communities = list
                        .sortWith((c1, c2) => c1.id.compareToIgnoreCase(c2.id) < 0)
                        .map(c => Community(c.id))
                        .toList
                val form = new RegisterForm
                form.communities = communities
                form.communitiesList = new ScalaObjectMapper().writeValueAsString(communities.map(_.name))
                result.setResult(new ModelAndView(v.registerView, "registerForm", form))
        }

        result
    }


    @RequestMapping(value = Array("/partner/register"), method = Array(RequestMethod.POST))
    def registerPost(
            @Valid form: RegisterForm,
            bindingResult: BindingResult) = {

        val errorModelAndView = new ModelAndView(v.registerView) with Errors
        form.communities = ScalaObjectMapper(form.communitiesList, classOf[List[String]])
                .map(name => Community(name, if (name == form.community) Some(true) else None))

        if (bindingResult.hasErrors) {
            errorModelAndView
        } else {

            val result = new DeferredResult[ModelAndView](null, errorModelAndView)

            mp(RegisterPartner(
                    form.userName,
                    form.email,
                    form.siteName,
                    form.siteUrl,
                    form.shortName,
                    form.community)).onSuccess {
                case RegisterPartnerResponse(_, Left(e)) =>
                    errorModelAndView.addError(e, Some("registerForm"))
                    result.setResult(errorModelAndView)
                case RegisterPartnerResponse(_, Right((partnerUser, partner))) =>
                    val mav = new ModelAndView(v.postRegisterView)
                    mav.addObject("partnerUser", partnerUser)
                    mav.addObject("partner", partner)
                    result.setResult(mav)
            }

            result
        }
    }

}
