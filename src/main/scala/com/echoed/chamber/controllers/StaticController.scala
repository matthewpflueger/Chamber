package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import scala.reflect.BeanProperty

import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView


@Controller
class StaticController {

    @BeanProperty var whatIsEchoedView: String = _
    @BeanProperty var contactUsView: String = _
    @BeanProperty var businessView: String = _
    @BeanProperty var privacyView: String = _
    @BeanProperty var termsView: String = _

    @RequestMapping(value = Array("/whatisechoed*"), method = Array(RequestMethod.GET))
    def howItWorks = new ModelAndView(whatIsEchoedView)

    @RequestMapping(value = Array("/contactus*"), method = Array(RequestMethod.GET))
    def contactUs = new ModelAndView(contactUsView)

    @RequestMapping(value = Array("/business*"), method = Array(RequestMethod.GET))
    def business = new ModelAndView(businessView)

    @RequestMapping(value = Array("/privacy*"), method = Array(RequestMethod.GET))
    def privacy = new ModelAndView(privacyView)

    @RequestMapping(value = Array("/terms*"), method = Array(RequestMethod.GET))
    def terms = new ModelAndView(termsView)

    @RequestMapping(value = Array("/story"), method = Array(RequestMethod.GET))
    def story = new ModelAndView("story")

    @RequestMapping(value = Array("/email"), method = Array(RequestMethod.GET))
    def email = new ModelAndView("email_template")
}
