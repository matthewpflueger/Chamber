package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView


@Controller
class StaticController extends EchoedController {

    @RequestMapping(value = Array("/about*"), method = Array(RequestMethod.GET))
    def howItWorks = new ModelAndView(v.whatIsEchoedView)

    @RequestMapping(value = Array("/101*"), method = Array(RequestMethod.GET))
    def storyTelling = new ModelAndView(v.storyTellingView)

    @RequestMapping(value = Array("/guidelines"), method = Array(RequestMethod.GET))
    def about = new ModelAndView(v.guidelinesView)

    @RequestMapping(value = Array("/contactus*"), method = Array(RequestMethod.GET))
    def contactUs = new ModelAndView(v.contactUsView)

    @RequestMapping(value = Array("/business*"), method = Array(RequestMethod.GET))
    def business = new ModelAndView(v.businessView)

    @RequestMapping(value = Array("/privacy*"), method = Array(RequestMethod.GET))
    def privacy = new ModelAndView(v.privacyView)

    @RequestMapping(value = Array("/terms*"), method = Array(RequestMethod.GET))
    def terms = new ModelAndView(v.termsView)

    @RequestMapping(value = Array("/robots.txt"), method = Array(RequestMethod.GET))
    def robots = new ModelAndView("robots.txt")
}
