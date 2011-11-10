package com.echoed.chamber.controllers

import com.echoed.chamber.domain.EchoPossibility
import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{CookieValue, RequestMapping, RequestMethod}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.chamber.services.EchoService
import org.springframework.web.servlet.ModelAndView


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
            echoPossibility: EchoPossibility,
            httpServletResponse: HttpServletResponse) = {
        echoPossibility.echoedUserId = echoedUserId
        echoPossibility.step = "button" //TODO externalize this...
        recordEchoPossibility(echoPossibility)
        new ModelAndView(buttonView)
    }

//    @RequestMapping(value = Array("/button"), method = Array(RequestMethod.GET))
//    def button(echoPossibility: EchoPossibility, httpServletResponse: HttpServletResponse) {
//        insertOrUpdate(echoPossibility, 1)
//        httpServletResponse.sendRedirect(buttonRedirectUrl)
//    }

    @RequestMapping(method = Array(RequestMethod.GET))
    def echo(
            echoPossibility: EchoPossibility,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {
//        insertOrUpdate(echoPossibility, 2)
        new ModelAndView(loginView)
    }

    def recordEchoPossibility(echoPossibility: EchoPossibility) {
        echoService.recordEchoPossibility(echoPossibility)
                .onComplete(_.value.get.fold(e => logger.error("Failed to record {} due to {}", echoPossibility, e),
                                             p => logger.debug("Recorded {}", p)))
                .onTimeout(_ => logger.error("Timeout recording {}", echoPossibility))
    }

//    private def insertOrUpdate(echoPossibility: EchoPossibility, step: Int) {
//        Future {
//            echoPossibility.step = step
//            echoPossibility.shownOn = new Date
//            retailerConfirmationDao.insertOrUpdate(echoPossibility)
//        }.onResult({case r => logger.debug("Inserted EchoPossibility record %s" format r)})
//         .onException({case e => logger.error("Error inserting EchoPossibility record %s" format e)})
//         .onTimeout(f => logger.warn("Future timed out %s" format f))
//    }


}