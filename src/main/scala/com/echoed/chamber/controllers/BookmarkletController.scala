package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import javax.annotation.Nullable
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials


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
