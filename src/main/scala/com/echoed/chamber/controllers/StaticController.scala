package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty

import com.echoed.chamber.services.echoeduser._
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._


@Controller
class StaticController {

    @BeanProperty var whatIsEchoedView: String = _
    @BeanProperty var contactUsView: String = _
    @BeanProperty var businessView: String = _

    @RequestMapping(value = Array("/whatisechoed*"), method = Array(RequestMethod.GET))
    def howItWorks = new ModelAndView(whatIsEchoedView)

    @RequestMapping(value = Array("/contactus*"), method = Array(RequestMethod.GET))
    def contactUs = new ModelAndView(contactUsView)

    @RequestMapping(value = Array("/business*"), method = Array(RequestMethod.GET))
    def business = new ModelAndView(businessView)

}
