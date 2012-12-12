package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.event.WidgetRequested
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import javax.annotation.Nullable
import com.echoed.chamber.services.partner._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.partner.FetchPartnerAndPartnerSettings
import com.echoed.chamber.services.event.WidgetRequested
import com.echoed.chamber.services.partner.FetchPartnerResponse
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.partner.FetchPartner
import com.echoed.chamber.services.partner.PartnerClientCredentials
import scala.Right


@Controller
@RequestMapping(Array("/bookmarklet"))
class BookmarkletController extends EchoedController {

    @RequestMapping(value = Array("/js"), method = Array(RequestMethod.GET))
    def bookmarklet(@Nullable eucc: EchoedUserClientCredentials) = {

        val modelAndView = new ModelAndView(v.bookmarkletJsView)
        modelAndView.addObject("echoedUserId", Option(eucc).map(_.id).getOrElse(""))
        modelAndView
    }

}
