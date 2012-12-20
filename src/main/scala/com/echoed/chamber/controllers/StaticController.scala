package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.email.{SendEmailResponse, SendEmail}
import scala.concurrent.ExecutionContext.Implicits.global


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

    @RequestMapping(value = Array("/contactus"), method = Array(RequestMethod.POST))
    def postContactUs(
            @RequestParam(value = "name", required = true) name: String,
            @RequestParam(value = "email", required = true) email: String,
            @RequestParam(value = "phone", required = true) phone: String,
            @RequestParam(value = "message", required = true) message: String) = {

        val result = new DeferredResult[ModelAndView](null, ErrorResult.timeout)
        val map = Map(
            "name" -> name,
            "email" -> email,
            "phone" -> phone,
            "message" -> message)
        mp(SendEmail("accountmanager@echoed.com", "Contact Us Inquiry", "email_contactus", map)).onSuccess {
            case SendEmailResponse(_, Right(b)) =>
                val modelAndView = new ModelAndView(v.contactUsView)
                modelAndView.addObject("submit", true)
                result.setResult(modelAndView)
        }
        result

    }

    @RequestMapping(value = Array("/websites*"), method = Array(RequestMethod.GET))
    def business = new ModelAndView(v.websitesView)

    @RequestMapping(value = Array("/plans*"), method = Array(RequestMethod.GET))
    def plans = new ModelAndView(v.plansView)

    @RequestMapping(value = Array("/privacy*"), method = Array(RequestMethod.GET))
    def privacy = new ModelAndView(v.privacyView)

    @RequestMapping(value = Array("/terms*"), method = Array(RequestMethod.GET))
    def terms = new ModelAndView(v.termsView)

    @RequestMapping(value = Array("/robots.txt"), method = Array(RequestMethod.GET))
    def robots = new ModelAndView("robots.txt")
}
