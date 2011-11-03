package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.services.facebook.FacebookServiceLocator
import reflect.BeanProperty


@Controller
@RequestMapping(Array("/facebook"))
class FacebookController {

    private val logger = LoggerFactory.getLogger(classOf[FacebookController])

    @Autowired @BeanProperty var facebookServiceLocator: FacebookServiceLocator = _

    @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
    def button(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse): ModelAndView = {

        val code = httpServletRequest.getParameter("code")

        logger.debug("Requesting FacebookService with code {}", code)

        facebookServiceLocator.getFacebookServiceWithCode(code)
                .onComplete(_.value.get.fold(_ => logger.error("Failed to receive FacebookService with code {}", code),
                                             _ => logger.debug("Received FacebookService with code {}", code)))
                .onTimeout(_ => logger.error("Timeout requesting FacebookService with code {}", code))

        new ModelAndView("test")
    }

}

