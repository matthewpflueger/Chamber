package com.echoed.chamber.controllers

import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import com.echoed.chamber.domain.RetailerConfirmation
import org.springframework.stereotype.Controller
import com.echoed.chamber.dao.RetailerConfirmationDao
import reflect.BeanProperty
import akka.dispatch.Future
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import java.util.Date
import org.slf4j.LoggerFactory


@Controller
@RequestMapping(Array("/echo"))
class EchoController extends {

    private val logger = LoggerFactory.getLogger(classOf[EchoController])

    @BeanProperty var retailerConfirmationDao: RetailerConfirmationDao = null
    @BeanProperty var buttonRedirectUrl: String = null
    @BeanProperty var loginRedirectUrl: String = null

    @RequestMapping(value = Array("/button"), method = Array(RequestMethod.GET))
    def button(retailerConfirmation: RetailerConfirmation, httpServletResponse: HttpServletResponse) {
        insertRetailerConfirmation(retailerConfirmation, 1)
        httpServletResponse.sendRedirect(buttonRedirectUrl)
    }

    @RequestMapping(method = Array(RequestMethod.GET))
    def echo(retailerConfirmation: RetailerConfirmation, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {
        insertRetailerConfirmation(retailerConfirmation, 2)
        httpServletResponse.sendRedirect(loginRedirectUrl)
    }

    private def insertRetailerConfirmation(retailerConfirmation: RetailerConfirmation, step: Int) {
        Future {
            retailerConfirmation.step = step
            retailerConfirmation.shownOn = new Date
            retailerConfirmationDao.insertRetailerConfirmation(retailerConfirmation)
        }.onResult({case r => logger.debug("Inserted RetailerConfirmation record %s" format r)})
         .onException({case e => logger.error("Error inserting RetailerConfirmation record %s" format e)})
         .onTimeout(f => logger.warn("Future timed out %s" format f))
    }


}