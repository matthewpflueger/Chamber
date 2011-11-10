package com.echoed.chamber.controllers

import com.echoed.chamber.domain.EchoPossibility
import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.chamber.services.EchoService
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.annotation.{CookieValue, RequestMapping, RequestMethod}
import org.springframework.web.bind.annotation.CookieValue._


@Controller
@RequestMapping(Array("/echo"))
class EchoController {

    private val logger = LoggerFactory.getLogger(classOf[EchoController])

    @BeanProperty var buttonView: String = null
    @BeanProperty var loginView: String = null
    @BeanProperty var echoService: EchoService = null

    @RequestMapping(value = Array("/button"), method = Array(RequestMethod.GET))
    def button(
            //TODO cookies should be encrypted
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            echoPossibility: EchoPossibility) = {
        echoPossibility.echoedUserId = echoedUserId
        echoPossibility.step = "button" //TODO externalize this...
        recordEchoPossibility(echoPossibility)
        new ModelAndView(buttonView)
    }

    @RequestMapping(method = Array(RequestMethod.GET))
    def echo(
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            echoPossibility: EchoPossibility,
            httpServletResponse: HttpServletResponse) = {
        echoPossibility.echoedUserId = echoedUserId
        echoPossibility.step = "login"
        recordEchoPossibility(echoPossibility)
        new ModelAndView(loginView)
    }

    def recordEchoPossibility(echoPossibility: EchoPossibility) {
        echoService.recordEchoPossibility(echoPossibility)
                .onComplete(_.value.get.fold(e => logger.error("Failed to record {} due to {}", echoPossibility, e),
                                             p => logger.debug("Recorded {}", p)))
                .onTimeout(_ => logger.error("Timeout recording {}", echoPossibility))
    }

}